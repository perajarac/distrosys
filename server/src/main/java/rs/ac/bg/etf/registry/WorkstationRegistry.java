package rs.ac.bg.etf.registry;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.bg.etf.proto.HeartbeatRequest;
import rs.ac.bg.etf.proto.RegisterWorkstationRequest;

/** Tracks registered workstations and their current load for job scheduling. */
public class WorkstationRegistry {

  private static final Logger log = LoggerFactory.getLogger(WorkstationRegistry.class);

  private final ConcurrentHashMap<String, WorkstationInfo> byId = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> endpointToId = new ConcurrentHashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);

  /**
   * Registers a workstation or rejects duplicate endpoints.
   *
   * @param request registration request from the workstation
   * @return assigned workstation id
   * @throws IllegalStateException if the endpoint is already registered
   */
  public synchronized String register(RegisterWorkstationRequest request) {
    String endpoint = request.getGrpcHost() + ":" + request.getGrpcPort();
    if (endpointToId.containsKey(endpoint)) {
      log.warn("Duplicate workstation registration at {}", endpoint);
      throw new IllegalStateException("Workstation already registered at " + endpoint);
    }
    String workstationId = "ws-" + nextId.getAndIncrement();
    WorkstationInfo info =
        new WorkstationInfo(
            workstationId,
            request.getHostname(),
            request.getGrpcHost(),
            request.getGrpcPort(),
            request.getParallelism());
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(request.getGrpcHost(), request.getGrpcPort())
            .usePlaintext()
            .build();
    info.setChannel(channel);
    byId.put(workstationId, info);
    endpointToId.put(endpoint, workstationId);
    log.info(
        "Registered workstation {} at {} (hostname={}, parallelism={})",
        workstationId,
        endpoint,
        request.getHostname(),
        request.getParallelism());
    return workstationId;
  }

  /**
   * Updates heartbeat metadata for a registered workstation.
   *
   * @param request heartbeat from the workstation
   * @return {@code true} if the workstation is known
   */
  public boolean heartbeat(HeartbeatRequest request) {
    WorkstationInfo info = byId.get(request.getWorkstationId());
    if (info == null) {
      return false;
    }
    info.setLastHeartbeat(java.time.Instant.now());
    info.setActiveSubjobs(request.getActiveSubjobs());
    info.setReportedLoad(request.getReportedLoad());
    return true;
  }

  /**
   * @return all currently registered workstations (MVP: all count as active)
   */
  public List<WorkstationInfo> getActiveWorkstations() {
    return new ArrayList<>(byId.values());
  }

  /**
   * @param workstationId workstation identifier
   * @return workstation metadata if registered
   */
  public Optional<WorkstationInfo> get(String workstationId) {
    return Optional.ofNullable(byId.get(workstationId));
  }

  /**
   * Selects the workstation with the lowest normalized load.
   *
   * @return least loaded workstation, if any are registered
   */
  public Optional<WorkstationInfo> pickLeastLoaded() {
    return byId.values().stream()
        .min(
            Comparator.comparingDouble(WorkstationInfo::loadRatio)
                .thenComparing(WorkstationInfo::getWorkstationId))
        .map(
            info -> {
              log.debug(
                  "Selected workstation {} with load ratio {}",
                  info.getWorkstationId(),
                  info.loadRatio());
              return info;
            });
  }

  /**
   * Increments the active sub-job counter for load tracking.
   *
   * @param workstationId workstation identifier
   */
  public void incrementLoad(String workstationId) {
    get(workstationId).ifPresent(info -> info.setActiveSubjobs(info.getActiveSubjobs() + 1));
  }

  /**
   * Decrements the active sub-job counter for load tracking.
   *
   * @param workstationId workstation identifier
   */
  public void decrementLoad(String workstationId) {
    get(workstationId)
        .ifPresent(info -> info.setActiveSubjobs(Math.max(0, info.getActiveSubjobs() - 1)));
  }

  /** Shuts down all managed gRPC channels. */
  public void shutdownChannels() {
    for (WorkstationInfo info : byId.values()) {
      ManagedChannel channel = info.getChannel();
      if (channel != null) {
        channel.shutdown();
      }
    }
  }

  /**
   * @return read-only view of registered workstations keyed by id
   */
  public Map<String, WorkstationInfo> snapshot() {
    return Map.copyOf(byId);
  }
}
