package rs.ac.bg.etf.kdp.simulation.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Initializes logging prerequisites for the simulation CLI.
 */
public final class LoggingBootstrap {

    private LoggingBootstrap() {
    }

    /**
     * Ensures the log directory exists before Logback writes rolling files.
     *
     * <p>Override defaults with {@code -Dlog.dir=...} before calling.
     */
    public static void init() {
        ensureLogDirectory();
    }

    private static void ensureLogDirectory() {
        String logDir = System.getProperty("log.dir", "logs");
        try {
            Files.createDirectories(Path.of(logDir));
        } catch (IOException e) {
            System.err.printf("Failed to create log directory %s: %s%n", logDir, e.getMessage());
        }
    }
}
