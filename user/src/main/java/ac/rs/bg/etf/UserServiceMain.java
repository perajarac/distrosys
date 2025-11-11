package ac.rs.bg.etf;

import ac.rs.bg.etf.proto.BagArg;
import ac.rs.bg.etf.proto.BagRet;
import ac.rs.bg.etf.proto.ServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class UserServiceMain {

    private static final String HOST = "localhost";
    private static final int PORT = 50051;

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        ServerServiceGrpc.ServerServiceBlockingStub stub = ServerServiceGrpc.newBlockingStub(channel);

        BagArg request = BagArg.newBuilder()
                .setNUM(1)
                .build();

        try {
            BagRet response = stub.sendBagTask(request);
            System.out.printf("Received %d result rows and %d task statuses%n",
                    response.getResultCount(), response.getTaskRetCount());
        } catch (StatusRuntimeException e) {
            System.err.printf("RPC failed: %s%n", e.getStatus());
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}