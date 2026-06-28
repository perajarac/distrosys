package rs.ac.bg.etf.registry;

import io.grpc.ManagedChannel;
import java.time.Instant;

/** Registered workstation metadata tracked by {@link WorkstationRegistry}. */
public class WorkstationInfo {

  private final String workstationId;
  private final String hostname;
  private final String grpcHost;
  private final int grpcPort;
  private final int parallelism;
  private Instant lastHeartbeat;
  private int activeSubjobs;
  private long reportedLoad;
  private ManagedChannel channel;

  /**
   * @param workstationId unique workstation identifier assigned by the server
   * @param hostname reported hostname
   * @param grpcHost gRPC host reachable from the server
   * @param grpcPort gRPC port for {@link rs.ac.bg.etf.proto.WorkstationServiceGrpc}
   * @param parallelism worker thread pool size on the workstation
   */
  public WorkstationInfo(
      String workstationId, String hostname, String grpcHost, int grpcPort, int parallelism) {
    this.workstationId = workstationId;
    this.hostname = hostname;
    this.grpcHost = grpcHost;
    this.grpcPort = grpcPort;
    this.parallelism = parallelism;
    this.lastHeartbeat = Instant.now();
  }

  /**
   * @return unique workstation identifier
   */
  public String getWorkstationId() {
    return workstationId;
  }

  /**
   * @return reported hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @return gRPC host reachable from the server
   */
  public String getGrpcHost() {
    return grpcHost;
  }

  /**
   * @return gRPC port
   */
  public int getGrpcPort() {
    return grpcPort;
  }

  /**
   * @return configured parallelism (thread pool size)
   */
  public int getParallelism() {
    return parallelism;
  }

  /**
   * @return timestamp of the last successful heartbeat
   */
  public Instant getLastHeartbeat() {
    return lastHeartbeat;
  }

  /**
   * @param lastHeartbeat timestamp of the last successful heartbeat
   */
  public void setLastHeartbeat(Instant lastHeartbeat) {
    this.lastHeartbeat = lastHeartbeat;
  }

  /**
   * @return number of sub-jobs currently running on this workstation
   */
  public int getActiveSubjobs() {
    return activeSubjobs;
  }

  /**
   * @param activeSubjobs number of sub-jobs currently running on this workstation
   */
  public void setActiveSubjobs(int activeSubjobs) {
    this.activeSubjobs = activeSubjobs;
  }

  /**
   * @return load metric reported by the workstation
   */
  public long getReportedLoad() {
    return reportedLoad;
  }

  /**
   * @param reportedLoad load metric reported by the workstation
   */
  public void setReportedLoad(long reportedLoad) {
    this.reportedLoad = reportedLoad;
  }

  /**
   * @return gRPC channel used to call the workstation
   */
  public ManagedChannel getChannel() {
    return channel;
  }

  /**
   * @param channel gRPC channel used to call the workstation
   */
  public void setChannel(ManagedChannel channel) {
    this.channel = channel;
  }

  /**
   * @return normalized load ratio used for scheduling decisions
   */
  public double loadRatio() {
    if (parallelism <= 0) {
      return activeSubjobs;
    }
    return (double) activeSubjobs / parallelism;
  }

  /**
   * @return endpoint key for duplicate detection
   */
  public String endpointKey() {
    return grpcHost + ":" + grpcPort;
  }
}
