package rs.ac.bg.etf;

import rs.ac.bg.etf.client.WorkstationClient;
import rs.ac.bg.etf.config.ServerConfig;
import rs.ac.bg.etf.job.JobStore;
import rs.ac.bg.etf.job.JobValidator;
import rs.ac.bg.etf.merge.ResultMerger;
import rs.ac.bg.etf.registry.WorkstationRegistry;
import rs.ac.bg.etf.scheduling.JobScheduler;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Server entry point — starts gRPC and text protocol services with wired dependencies.
 */
public class ServerServiceMain {

    private static final Logger log = LoggerFactory.getLogger(ServerServiceMain.class);

    /**
     * Starts the distributed simulation server.
     *
     * @param args unused
     * @throws IOException          if server sockets cannot be opened
     * @throws InterruptedException if shutdown is interrupted
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        LoggingBootstrap.init();
        ServerConfig config = ServerConfig.load();

        JobStore jobStore = new JobStore(Path.of(config.getJobsDir()));
        jobStore.loadPersistedJobs();

        WorkstationRegistry workstationRegistry = new WorkstationRegistry();
        JobValidator jobValidator = new JobValidator();
        WorkstationClient workstationClient = new WorkstationClient();
        ResultMerger resultMerger = new ResultMerger();
        ExecutorService schedulerExecutor = Executors.newFixedThreadPool(config.getSchedulerPoolSize());
        JobScheduler jobScheduler = new JobScheduler(
                jobStore, workstationRegistry, workstationClient, resultMerger, schedulerExecutor);

        ServerServiceImpl service = new ServerServiceImpl(
                jobStore, jobValidator, jobScheduler, workstationRegistry);

        TextProtocolServer textProtocolServer = new TextProtocolServer(config.getTextPort());
        textProtocolServer.start();

        Server grpcServer = ServerBuilder.forPort(config.getGrpcPort())
                .addService(service)
                .build()
                .start();

        log.info("gRPC server running on port {}", config.getGrpcPort());
        log.info("Text protocol server running on port {}", config.getTextPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server");
            textProtocolServer.stop();
            grpcServer.shutdown();
            schedulerExecutor.shutdown();
            workstationRegistry.shutdownChannels();
            try {
                grpcServer.awaitTermination(5, TimeUnit.SECONDS);
                schedulerExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        grpcServer.awaitTermination();
    }
}
