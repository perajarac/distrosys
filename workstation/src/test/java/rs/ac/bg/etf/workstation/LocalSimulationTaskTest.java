package rs.ac.bg.etf.workstation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import rs.ac.bg.etf.proto.ComponentState;
import rs.ac.bg.etf.proto.SimulationType;
import rs.ac.bg.etf.proto.SubJobRequest;

import org.junit.jupiter.api.Test;

import rs.ac.bg.etf.kdp.simulation.io.NetlistLoader;
import rs.ac.bg.etf.kdp.simulation.io.NetlistWriter;
import rs.ac.bg.etf.kdp.simulation.io.SimulatorFactory;
import rs.ac.bg.etf.sleep.simulation.Netlist;
import rs.ac.bg.etf.sleep.simulation.Simulator;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verifies {@link LocalSimulationTask} produces the same states as a local simulation run.
 */
class LocalSimulationTaskTest {

    @Test
    void miniDatasetMatchesLocalRunner() throws Exception {
        Path miniDir = resourceDirectory("mini");
        List<String> componentLines = Files.readAllLines(miniDir.resolve("komponente.txt"));
        List<String> connectionLines = Files.readAllLines(miniDir.resolve("veze.txt"));
        long endTime = 100L;

        Path outputPath = Files.createTempFile("local-sim-task", ".txt");
        Netlist<Object> netlist = NetlistLoader.load(
                miniDir.resolve("komponente.txt").toString(),
                miniDir.resolve("veze.txt").toString());
        Simulator<Object> simulator = (Simulator<Object>) SimulatorFactory.create(
                rs.ac.bg.etf.kdp.simulation.io.SimulationType.OPTIMISTIC, 1);
        simulator.setNetlist(netlist);
        simulator.init();
        while (simulator.getlTime() < endTime) {
            simulator.execute();
        }
        NetlistWriter.write(netlist, outputPath.toString());
        List<String> expectedLines = Files.readAllLines(outputPath);

        Map<Long, String> componentOwner = new HashMap<>();
        for (String line : componentLines) {
            long id = Long.parseLong(line.trim().split("\\s+")[0]);
            componentOwner.put(id, "ws-1");
        }

        SubJobRequest request = SubJobRequest.newBuilder()
                .setJobId(1L)
                .setSubjobId(1L)
                .addAllComponentLines(componentLines)
                .addAllConnectionLines(connectionLines)
                .setSimType(SimulationType.OPTIMISTIC)
                .setEndTime(endTime)
                .setLocalWorkstationId("ws-1")
                .putAllComponentOwner(componentOwner)
                .build();

        WorkstationState state = new WorkstationState(1);
        state.setWorkstationId("ws-1");
        SubJobResult result = new LocalSimulationTask(request, state).call();

        assertTrue(result.isSuccess(), result.getErrorMessage());
        List<ComponentState> states = result.getComponentStates();
        assertEquals(expectedLines.size(), states.size());

        for (int i = 0; i < states.size(); i++) {
            String actual = String.join(" ", states.get(i).getStateValuesList());
            assertEquals(expectedLines.get(i), actual);
        }
    }

    private static Path resourceDirectory(String name) throws Exception {
        URL url = LocalSimulationTaskTest.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + name);
        }
        return Path.of(url.toURI());
    }
}
