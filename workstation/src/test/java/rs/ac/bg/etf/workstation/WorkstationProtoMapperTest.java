package rs.ac.bg.etf.workstation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import rs.ac.bg.etf.proto.SimulationType;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WorkstationProtoMapper}.
 */
class WorkstationProtoMapperTest {

    @Test
    void simulationTypeRoundTrip() {
        assertEquals(SimulationType.OPTIMISTIC,
                WorkstationProtoMapper.toProto(rs.ac.bg.etf.kdp.simulation.io.SimulationType.OPTIMISTIC));
        assertEquals(rs.ac.bg.etf.kdp.simulation.io.SimulationType.OPTIMISTIC,
                WorkstationProtoMapper.toCore(SimulationType.OPTIMISTIC));
    }
}
