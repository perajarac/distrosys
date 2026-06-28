package rs.ac.bg.etf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** Initializes SLF4J/Logback and bridges java.util.logging to SLF4J for gRPC. */
public final class LoggingBootstrap {

  private LoggingBootstrap() {}

  /**
   * Installs the JUL-to-SLF4J bridge and ensures the log directory exists.
   *
   * <p>Override defaults with {@code -Dlog.dir=...} before calling.
   */
  public static void init() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
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
