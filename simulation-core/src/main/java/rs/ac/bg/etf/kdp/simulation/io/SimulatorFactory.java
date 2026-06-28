package rs.ac.bg.etf.kdp.simulation.io;

import rs.ac.bg.etf.sleep.simulation.Simulator;
import rs.ac.bg.etf.sleep.simulation.SimulatorMultithread;
import rs.ac.bg.etf.sleep.simulation.SimulatorOptimistic;
import rs.ac.bg.etf.sleep.simulation.SimulatorSinglethread;

/**
 * Creates {@link Simulator} instances for a given {@link SimulationType}.
 */
public final class SimulatorFactory {

    private SimulatorFactory() {
    }

    /**
     * Instantiates the simulator implementation that matches the requested type.
     *
     * @param type execution strategy
     * @param id simulator identifier passed to the concrete constructor
     * @return configured simulator (netlist and queue are not yet initialized)
     */
    public static Simulator<?> create(SimulationType type, int id) {
        return switch (type) {
            case OPTIMISTIC -> new SimulatorOptimistic<>(id);
            case SINGLETHREAD -> new SimulatorSinglethread<>(id);
            case MULTITHREAD -> new SimulatorMultithread<>(id);
        };
    }
}
