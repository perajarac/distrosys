package rs.ac.bg.etf.workstation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Runtime state for a workstation process: identity, load, and active simulations. */
public class WorkstationState {

  private volatile String workstationId;
  private final int parallelism;
  private final AtomicInteger activeSubjobs = new AtomicInteger();
  private final ConcurrentHashMap<Long, ActiveSimulation> activeSimulations =
      new ConcurrentHashMap<>();

  /**
   * @param parallelism configured sub-job thread pool size
   */
  public WorkstationState(int parallelism) {
    this.parallelism = parallelism;
  }

  /**
   * @param workstationId identifier assigned by the server at registration
   */
  public void setWorkstationId(String workstationId) {
    this.workstationId = workstationId;
  }

  /**
   * @return workstation identifier assigned by the server
   */
  public String getWorkstationId() {
    return workstationId;
  }

  /**
   * @return configured parallelism
   */
  public int getParallelism() {
    return parallelism;
  }

  /** Increments the active sub-job counter. */
  public void incrementActive() {
    activeSubjobs.incrementAndGet();
  }

  /** Decrements the active sub-job counter. */
  public void decrementActive() {
    activeSubjobs.decrementAndGet();
  }

  /**
   * @return number of sub-jobs currently executing
   */
  public int getActiveSubjobs() {
    return activeSubjobs.get();
  }

  /**
   * @return load metric reported in heartbeats
   */
  public long getReportedLoad() {
    return activeSubjobs.get();
  }

  /**
   * Registers a running simulation for remote event routing.
   *
   * @param jobId parent job identifier
   * @param simulation active simulation handle
   */
  public void registerSimulation(long jobId, ActiveSimulation simulation) {
    activeSimulations.put(jobId, simulation);
  }

  /**
   * Removes a completed simulation registration.
   *
   * @param jobId parent job identifier
   */
  public void unregisterSimulation(long jobId) {
    activeSimulations.remove(jobId);
  }

  /**
   * Looks up an active simulation by job id.
   *
   * @param jobId parent job identifier
   * @return active simulation if present
   */
  public ActiveSimulation getSimulation(long jobId) {
    return activeSimulations.get(jobId);
  }
}
