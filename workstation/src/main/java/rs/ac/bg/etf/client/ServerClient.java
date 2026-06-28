package rs.ac.bg.etf.client;

import rs.ac.bg.etf.proto.HeartbeatRequest;
import rs.ac.bg.etf.proto.HeartbeatResponse;
import rs.ac.bg.etf.proto.RegisterWorkstationRequest;
import rs.ac.bg.etf.proto.RegisterWorkstationResponse;
import rs.ac.bg.etf.proto.ServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC client for workstation registration and heartbeat RPCs on the server.
 */
public class ServerClient {

    private static final Logger log = LoggerFactory.getLogger(ServerClient.class);

    private final ManagedChannel channel;
    private final ServerServiceGrpc.ServerServiceBlockingStub stub;

    /**
     * @param serverHost server gRPC host
     * @param serverPort server gRPC port
     */
    public ServerClient(String serverHost, int serverPort) {
        this.channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext()
                .build();
        this.stub = ServerServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Registers this workstation with the server.
     *
     * @param hostname    reported hostname
     * @param grpcHost    reachable gRPC host for inbound workstation RPCs
     * @param grpcPort    gRPC listen port
     * @param parallelism sub-job thread pool size
     * @return assigned workstation id
     */
    public String register(String hostname, String grpcHost, int grpcPort, int parallelism) {
        RegisterWorkstationResponse response = stub.registerWorkstation(RegisterWorkstationRequest.newBuilder()
                .setHostname(hostname)
                .setGrpcHost(grpcHost)
                .setGrpcPort(grpcPort)
                .setParallelism(parallelism)
                .build());
        if (!response.getAccepted()) {
            log.error("Registration rejected: {}", response.getMessage());
            throw new IllegalStateException("Registration rejected: " + response.getMessage());
        }
        log.info("Registered with server as workstation {}", response.getWorkstationId());
        return response.getWorkstationId();
    }

    /**
     * Sends a heartbeat to the server.
     *
     * @param workstationId  registered workstation id
     * @param activeSubjobs  number of running sub-jobs
     * @param reportedLoad   load metric
     * @return {@code true} if the server acknowledged the heartbeat
     */
    public boolean heartbeat(String workstationId, int activeSubjobs, long reportedLoad) {
        HeartbeatResponse response = stub.heartbeat(HeartbeatRequest.newBuilder()
                .setWorkstationId(workstationId)
                .setActiveSubjobs(activeSubjobs)
                .setReportedLoad(reportedLoad)
                .build());
        return response.getAcknowledged();
    }

    /**
     * Shuts down the server channel.
     */
    public void shutdown() {
        channel.shutdown();
    }
}
