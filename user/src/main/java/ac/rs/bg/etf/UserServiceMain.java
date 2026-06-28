package ac.rs.bg.etf;

import ac.rs.bg.etf.proto.BagArg;
import ac.rs.bg.etf.proto.BagRet;
import ac.rs.bg.etf.proto.JobFilePaths;
import ac.rs.bg.etf.proto.ServerServiceGrpc;
import ac.rs.bg.etf.proto.SimulationType;
import ac.rs.bg.etf.proto.SubmitJobRequest;
import ac.rs.bg.etf.proto.SubmitJobResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

/**
 * Minimal gRPC client for smoke-testing {@link ServerServiceGrpc} stubs.
 */
public class UserServiceMain {

    private static final String HOST = "localhost";
    private static final int PORT = 50051;

    public static void main(String[] args) throws InterruptedException {
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
            System.out.printf("SubmitJob: job_id=%d status=%s message=%s%n",
                    submitResponse.getJobId(),
                    submitResponse.getStatus(),
                    submitResponse.getMessage());

            BagArg bagRequest = BagArg.newBuilder()
                    .setNUM(1)
                    .build();
            BagRet bagResponse = stub.sendBagTask(bagRequest);
            System.out.printf("SendBagTask: %d result rows, %d task statuses%n",
                    bagResponse.getResultCount(), bagResponse.getTaskRetCount());
        } catch (StatusRuntimeException e) {
            System.err.printf("RPC failed: %s%n", e.getStatus());
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
