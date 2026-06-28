package rs.ac.bg.etf.kdp.simulation.io;

/**
 * Discrete-event simulation execution strategy.
 *
 * <p>Used by {@link SimulatorFactory} and {@link SimulationRunner} to select
 * the concrete {@link rs.ac.bg.etf.sleep.simulation.Simulator} implementation.
 */
public enum SimulationType {
    /** Optimistic parallel execution (default for golden tests). */
    OPTIMISTIC,
    /** Single-threaded sequential execution. */
    SINGLETHREAD,
    /** Multi-threaded execution (stub implementation). */
    MULTITHREAD;

    /**
     * Parses a simulation type from a CLI or config string.
     *
     * @param value enum name, case-insensitive (e.g. {@code "optimistic"})
     * @return matching simulation type
     * @throws IllegalArgumentException if the value does not name a valid constant
     */
    public static SimulationType fromString(String value) {
        return SimulationType.valueOf(value.trim().toUpperCase());
    }
}
