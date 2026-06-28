package rs.ac.bg.etf.workstation;

import rs.ac.bg.etf.proto.Body;
import rs.ac.bg.etf.proto.ComponentState;
import rs.ac.bg.etf.proto.Field;
import rs.ac.bg.etf.proto.SimEvent;
import rs.ac.bg.etf.proto.SimulationType;

import rs.ac.bg.etf.sleep.simulation.Event;
import rs.ac.bg.etf.sleep.simulation.Netlist;
import rs.ac.bg.etf.sleep.simulation.SimComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Converts between protobuf messages and simulation-core types on the workstation.
 */
public final class WorkstationProtoMapper {

    private WorkstationProtoMapper() {
    }

    /**
     * Maps a protobuf simulation type to the simulation-core enum.
     *
     * @param simType protobuf simulation type
     * @return matching simulation-core type
     * @throws IllegalArgumentException if the type is unspecified or unknown
     */
    public static rs.ac.bg.etf.kdp.simulation.io.SimulationType toCore(SimulationType simType) {
        return switch (simType) {
            case OPTIMISTIC -> rs.ac.bg.etf.kdp.simulation.io.SimulationType.OPTIMISTIC;
            case SINGLETHREAD -> rs.ac.bg.etf.kdp.simulation.io.SimulationType.SINGLETHREAD;
            case MULTITHREAD -> rs.ac.bg.etf.kdp.simulation.io.SimulationType.MULTITHREAD;
            default -> throw new IllegalArgumentException("Unspecified simulation type: " + simType);
        };
    }

    /**
     * Maps a simulation-core simulation type to the protobuf enum.
     *
     * @param simType simulation-core simulation type
     * @return matching protobuf type
     */
    public static SimulationType toProto(rs.ac.bg.etf.kdp.simulation.io.SimulationType simType) {
        return switch (simType) {
            case OPTIMISTIC -> SimulationType.OPTIMISTIC;
            case SINGLETHREAD -> SimulationType.SINGLETHREAD;
            case MULTITHREAD -> SimulationType.MULTITHREAD;
        };
    }

    /**
     * Extracts component states for the given partition lines, sorted by component id.
     *
     * @param netlist        simulated netlist
     * @param componentLines raw component lines owned by this partition
     * @return protobuf component states
     */
    public static List<ComponentState> toComponentStates(Netlist<?> netlist, List<String> componentLines) {
        List<Long> ids = new ArrayList<>();
        for (String line : componentLines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            ids.add(Long.parseLong(line.trim().split("\\s+")[0]));
        }
        ids.sort(Comparator.naturalOrder());

        List<ComponentState> states = new ArrayList<>();
        for (Long id : ids) {
            SimComponent<?> component = netlist.getComponents().get(id);
            if (component == null) {
                throw new IllegalStateException("Missing component id " + id + " in netlist");
            }
            ComponentState.Builder builder = ComponentState.newBuilder().setComponentId(id);
            for (String value : component.getState()) {
                builder.addStateValues(value);
            }
            states.add(builder.build());
        }
        return states;
    }

    /**
     * Converts a protobuf event and field payload into a simulation {@link Event}.
     *
     * @param simEvent protobuf routing metadata
     * @param payload  protobuf field payload
     * @return simulation event with attached data
     */
    public static Event<rs.ac.bg.etf.kdp.simulation.components.Field> toEvent(
            SimEvent simEvent, Field payload) {
        Event<rs.ac.bg.etf.kdp.simulation.components.Field> event = new Event<>(
                simEvent.getLTimeCreated(),
                simEvent.getLTime(),
                simEvent.getSrcId(),
                simEvent.getSrcPort(),
                simEvent.getDstId(),
                simEvent.getDstPort());
        event.setId(simEvent.getId());
        event.setStatus(simEvent.getStatus());
        event.setData(toCoreField(payload));
        return event;
    }

    /**
     * Converts a simulation event into protobuf routing metadata.
     *
     * @param event simulation event
     * @return protobuf event metadata
     */
    public static SimEvent toProtoEvent(Event<rs.ac.bg.etf.kdp.simulation.components.Field> event) {
        return SimEvent.newBuilder()
                .setId(event.getId())
                .setStatus(event.getStatus())
                .setLTimeCreated(event.getlTimeCreated())
                .setLTime(event.getlTime())
                .setSrcId(event.getSrcID())
                .setSrcPort(event.getSrcPort())
                .setDstId(event.getDstID())
                .setDstPort(event.getDstPort())
                .build();
    }

    /**
     * Converts a simulation field payload to protobuf.
     *
     * @param field simulation field
     * @return protobuf field message
     */
    public static Field toProtoField(rs.ac.bg.etf.kdp.simulation.components.Field field) {
        Field.Builder builder = Field.newBuilder()
                .setSerialVersionUid(1L)
                .setGAMA(6.674 / 100000000000.0)
                .setIteration(field.getIteration())
                .setTime(field.getTime())
                .setInterval(field.getInterval());
        for (rs.ac.bg.etf.kdp.simulation.components.Body body : field.getCoordinates()) {
            builder.addCoordinates(toProtoBody(body));
        }
        for (Integer index : field.getIndexes()) {
            builder.addIndexes(index);
        }
        return builder.build();
    }

    /**
     * Converts a protobuf field payload to simulation-core.
     *
     * @param field protobuf field message
     * @return simulation field
     */
    public static rs.ac.bg.etf.kdp.simulation.components.Field toCoreField(Field field) {
        rs.ac.bg.etf.kdp.simulation.components.Field result =
                new rs.ac.bg.etf.kdp.simulation.components.Field();
        result.setIteration(field.getIteration());
        result.setTime(field.getTime());
        result.setInterval(field.getInterval());
        result.setCoordinates(new LinkedList<>());
        for (Body body : field.getCoordinatesList()) {
            result.getCoordinates().add(toCoreBody(body));
        }
        result.setIndexes(new LinkedList<>());
        for (int index : field.getIndexesList()) {
            result.getIndexes().add(index);
        }
        return result;
    }

    private static Body toProtoBody(rs.ac.bg.etf.kdp.simulation.components.Body body) {
        return Body.newBuilder()
                .setId(body.getId())
                .setM((int) body.getM())
                .setX(body.getX())
                .setY(body.getY())
                .setZ(body.getZ())
                .setVx(body.getVx())
                .setVy(body.getVy())
                .setVz(body.getVz())
                .build();
    }

    private static rs.ac.bg.etf.kdp.simulation.components.Body toCoreBody(Body body) {
        rs.ac.bg.etf.kdp.simulation.components.Body result = new rs.ac.bg.etf.kdp.simulation.components.Body();
        result.setId(body.getId());
        result.setM(body.getM());
        result.setX(body.getX());
        result.setY(body.getY());
        result.setZ(body.getZ());
        result.setVx(body.getVx());
        result.setVy(body.getVy());
        result.setVz(body.getVz());
        return result;
    }
}
