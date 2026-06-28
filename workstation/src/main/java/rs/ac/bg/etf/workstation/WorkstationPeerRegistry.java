package rs.ac.bg.etf.workstation;

import rs.ac.bg.etf.proto.ForwardEventRequest;
import rs.ac.bg.etf.proto.WorkstationEndpoint;
import rs.ac.bg.etf.proto.WorkstationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import rs.ac.bg.etf.kdp.simulation.components.Field;
import rs.ac.bg.etf.sleep.simulation.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC clients for peer workstations used by {@link SimBufferNetwork}.
 */
public class WorkstationPeerRegistry {

    private static final Logger log = LoggerFactory.getLogger(WorkstationPeerRegistry.class);

    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final Map<String, WorkstationServiceGrpc.WorkstationServiceBlockingStub> stubs = new ConcurrentHashMap<>();

    /**
     * Registers peer endpoints from a sub-job request.
     *
     * @param endpoints map of workstation id to gRPC endpoint
     */
    public void registerPeers(Map<String, WorkstationEndpoint> endpoints) {
        for (Map.Entry<String, WorkstationEndpoint> entry : endpoints.entrySet()) {
            String workstationId = entry.getKey();
            WorkstationEndpoint endpoint = entry.getValue();
            channels.computeIfAbsent(workstationId, id -> ManagedChannelBuilder
                    .forAddress(endpoint.getGrpcHost(), endpoint.getGrpcPort())
                    .usePlaintext()
                    .build());
            stubs.computeIfAbsent(workstationId, id ->
                    WorkstationServiceGrpc.newBlockingStub(channels.get(id)));
        }
    }

    /**
     * Forwards an event to a remote workstation partition.
     *
     * @param targetWorkstationId destination workstation id
     * @param jobId               parent job identifier
     * @param event               simulation event to forward
     */
    public void forwardEvent(String targetWorkstationId, long jobId, Event<Field> event) {
        WorkstationServiceGrpc.WorkstationServiceBlockingStub stub = stubs.get(targetWorkstationId);
        if (stub == null) {
            log.error("No peer stub for workstation {}", targetWorkstationId);
            throw new IllegalStateException("No peer stub for workstation " + targetWorkstationId);
        }
        ForwardEventRequest request = ForwardEventRequest.newBuilder()
                .setJobId(jobId)
                .setTargetWorkstationId(targetWorkstationId)
                .setEvent(WorkstationProtoMapper.toProtoEvent(event))
                .setPayload(WorkstationProtoMapper.toProtoField(event.getData()))
                .build();
        stub.forwardEvent(request);
    }

    /**
     * Closes all peer channels opened for the current sub-job.
     */
    public void shutdown() {
        for (ManagedChannel channel : channels.values()) {
            channel.shutdown();
        }
        channels.clear();
        stubs.clear();
    }
}
