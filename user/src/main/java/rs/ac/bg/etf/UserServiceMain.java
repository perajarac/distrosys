package rs.ac.bg.etf;

import rs.ac.bg.etf.proto.BagArg;
import rs.ac.bg.etf.proto.BagRet;
import rs.ac.bg.etf.proto.JobFilePaths;
import rs.ac.bg.etf.proto.ServerServiceGrpc;
import rs.ac.bg.etf.proto.SimulationType;
import rs.ac.bg.etf.proto.SubmitJobRequest;
import rs.ac.bg.etf.proto.SubmitJobResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Minimal gRPC client for smoke-testing {@link ServerServiceGrpc} stubs.
 */
public class UserServiceMain {

    private static final Logger log = LoggerFactory.getLogger(UserServiceMain.class);
    private static final String HOST = "localhost";
    private static final int PORT = 50051;

    public static void main(String[] args) throws InterruptedException {
        LoggingBootstrap.init();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        ServerServiceGrpc.ServerServiceBlockingStub stub = ServerServiceGrpc.newBlockingStub(channel);

        try {
            SubmitJobRequest submitRequest = SubmitJobRequest.newBuilder()
                    .setPaths(JobFilePaths.newBuilder()
                            .setComponentsPath("komponente.txt")
                            .setConnectionsPath("veze.txt")
                            .build())
                    .setSimType(SimulationType.OPTIMISTIC)
                    .setEndTime(100L)
                    .setClientOutputName("output.txt")
                    .build();

            SubmitJobResponse submitResponse = stub.submitJob(submitRequest);
            log.info("SubmitJob: job_id={} status={} message={}",
                    submitResponse.getJobId(),
                    submitResponse.getStatus(),
                    submitResponse.getMessage());

            BagArg bagRequest = BagArg.newBuilder()
                    .setNUM(1)
                    .build();
            BagRet bagResponse = stub.sendBagTask(bagRequest);
            log.info("SendBagTask: {} result rows, {} task statuses",
                    bagResponse.getResultCount(), bagResponse.getTaskRetCount());
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus(), e);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
