package rs.ac.bg.etf;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.bg.etf.client.ServerClient;
import rs.ac.bg.etf.config.WorkstationConfig;
import rs.ac.bg.etf.workstation.HeartbeatSender;
import rs.ac.bg.etf.workstation.SubJobExecutor;
import rs.ac.bg.etf.workstation.WorkstationState;

/** Workstation entry point — registers with the server and serves sub-job execution RPCs. */
public class WorkstationMain {

  private static final Logger log = LoggerFactory.getLogger(WorkstationMain.class);

  /**
   * Starts the workstation process.
   *
   * @param args {@code <serverHost> <serverPort> <parallelism> [--headless] [--grpc-port PORT]
   *     [--grpc-host HOST]}
   * @throws IOException if sockets cannot be opened
   * @throws InterruptedException if shutdown is interrupted
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    LoggingBootstrap.init();
    WorkstationConfig config = WorkstationConfig.fromArgs(args);

    WorkstationState state = new WorkstationState(config.getParallelism());
    SubJobExecutor executor = new SubJobExecutor(config.getParallelism());
    ServerClient serverClient = new ServerClient(config.getServerHost(), config.getServerPort());

    WorkstationServiceImpl service = new WorkstationServiceImpl(state, executor);
    Server grpcServer =
        ServerBuilder.forPort(config.getGrpcPort())
            .addService((BindableService) service)
            .build()
            .start();

    String hostname = InetAddress.getLocalHost().getHostName();
    String workstationId =
        serverClient.register(
            hostname, config.getGrpcHost(), config.getGrpcPort(), config.getParallelism());
    state.setWorkstationId(workstationId);

    HeartbeatSender heartbeatSender =
        new HeartbeatSender(serverClient, state, config.getHeartbeatIntervalSec());
    heartbeatSender.start();

    log.info(
        "Workstation {} listening on {}:{} (server {}:{}, parallelism {})",
        workstationId,
        config.getGrpcHost(),
        config.getGrpcPort(),
        config.getServerHost(),
        config.getServerPort(),
        config.getParallelism());
    if (config.isHeadless()) {
      log.info("Running in headless mode");
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Shutting down workstation");
                  heartbeatSender.stop();
                  grpcServer.shutdown();
                  executor.shutdown();
                  serverClient.shutdown();
                  try {
                    grpcServer.awaitTermination(5, TimeUnit.SECONDS);
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                  }
                }));

    grpcServer.awaitTermination();
  }
}
