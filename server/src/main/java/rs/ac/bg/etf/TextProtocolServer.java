package rs.ac.bg.etf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Line-based TCP server that logs unsolicited client input without affecting job processing.
 *
 * <p>Used for public test 4 — arbitrary text connections must not interfere with gRPC.
 */
public class TextProtocolServer implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(TextProtocolServer.class);

  private final int port;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private ServerSocket serverSocket;
  private Thread serverThread;

  /**
   * @param port TCP listen port (typically 50050)
   */
  public TextProtocolServer(int port) {
    this.port = port;
  }

  /**
   * Starts the text protocol server on a background daemon thread.
   *
   * @throws IOException if the listen socket cannot be created
   */
  public void start() throws IOException {
    serverSocket = new ServerSocket(port);
    serverThread = new Thread(this, "text-protocol-server");
    serverThread.setDaemon(true);
    serverThread.start();
    log.info("Text protocol server listening on port {}", port);
  }

  /** Stops accepting connections and closes the listen socket. */
  public void stop() {
    running.set(false);
    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        log.warn("Failed to close text protocol server socket", e);
      }
    }
    if (serverThread != null) {
      serverThread.interrupt();
    }
  }

  @Override
  public void run() {
    while (running.get()) {
      try {
        Socket client = serverSocket.accept();
        Thread handler = new Thread(() -> handleClient(client), "text-client-" + client.getPort());
        handler.setDaemon(true);
        handler.start();
      } catch (IOException e) {
        if (running.get()) {
          log.warn("Text protocol accept failed", e);
        }
      }
    }
  }

  private void handleClient(Socket client) {
    try (Socket ignored = client;
        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        log.debug("Text protocol received: {}", line);
      }
    } catch (IOException e) {
      log.debug("Text protocol client disconnected", e);
    }
  }
}
