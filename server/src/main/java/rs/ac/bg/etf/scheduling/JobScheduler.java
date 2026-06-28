package rs.ac.bg.etf.scheduling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.bg.etf.client.SubJobDispatcher;
import rs.ac.bg.etf.job.Job;
import rs.ac.bg.etf.job.JobStore;
import rs.ac.bg.etf.job.SubJob;
import rs.ac.bg.etf.merge.ResultMerger;
import rs.ac.bg.etf.proto.ComponentState;
import rs.ac.bg.etf.proto.JobStatus;
import rs.ac.bg.etf.proto.SubJobRequest;
import rs.ac.bg.etf.proto.SubJobResponse;
import rs.ac.bg.etf.proto.SubJobStatus;
import rs.ac.bg.etf.proto.WorkstationEndpoint;
import rs.ac.bg.etf.registry.WorkstationInfo;
import rs.ac.bg.etf.registry.WorkstationRegistry;

/** Partitions jobs into sub-jobs and dispatches them to workstations asynchronously. */
public class JobScheduler {

  private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

  private final JobStore jobStore;
  private final WorkstationRegistry workstationRegistry;
  private final SubJobDispatcher workstationClient;
  private final ResultMerger resultMerger;
  private final ExecutorService executor;

  /**
   * @param jobStore persisted job registry
   * @param workstationRegistry active workstation registry
   * @param workstationClient dispatcher for workstation sub-jobs
   * @param resultMerger merges sub-job outputs into final result bytes
   * @param executor background executor for async scheduling
   */
  public JobScheduler(
      JobStore jobStore,
      WorkstationRegistry workstationRegistry,
      SubJobDispatcher workstationClient,
      ResultMerger resultMerger,
      ExecutorService executor) {
    this.jobStore = jobStore;
    this.workstationRegistry = workstationRegistry;
    this.workstationClient = workstationClient;
    this.resultMerger = resultMerger;
    this.executor = executor;
  }

  /**
   * Validates workstation availability, builds a single sub-job (K=1), and runs dispatch
   * asynchronously.
   *
   * @param job job already stored in {@link JobStore}
   * @throws IOException if job state cannot be persisted
   */
  public void schedule(Job job) throws IOException {
    Optional<WorkstationInfo> workstation = workstationRegistry.pickLeastLoaded();
    if (workstation.isEmpty()) {
      log.warn("No active workstations available for job {}", job.getId());
      jobStore.updateStatus(job.getId(), JobStatus.FAILED, "no active workstations");
      return;
    }

    SubJob subJob = buildSingleSubJob(job, workstation.get());
    job.setSubJobs(List.of(subJob));
    job.setStatus(JobStatus.SCHEDULED);
    jobStore.update(job);

    log.info(
        "Scheduled job {} on workstation {}", job.getId(), workstation.get().getWorkstationId());
    executor.submit(() -> runJob(job.getId(), workstation.get()));
  }

  /**
   * Estimates partition complexity for future multi-workstation scheduling.
   *
   * @param numComponents number of netlist components
   * @param avgSimTime average simulated time per component
   * @param crossEdges number of cross-partition connections
   * @return complexity score
   */
  static long estimateComplexity(int numComponents, long avgSimTime, int crossEdges) {
    return (long) numComponents * avgSimTime + crossEdges;
  }

  private void runJob(long jobId, WorkstationInfo workstation) {
    try {
      Job job = jobStore.get(jobId).orElseThrow();
      if (job.getStatus() == JobStatus.ABORTED) {
        log.info("Job {} aborted before execution", jobId);
        return;
      }
      job.setStatus(JobStatus.RUNNING);
      jobStore.update(job);
      log.info("Running job {} on workstation {}", jobId, workstation.getWorkstationId());

      for (SubJob subJob : job.getSubJobs()) {
        if (jobStore.get(jobId).map(j -> j.getStatus() == JobStatus.ABORTED).orElse(true)) {
          log.info("Job {} aborted during sub-job execution", jobId);
          return;
        }
        workstationRegistry.incrementLoad(workstation.getWorkstationId());
        try {
          subJob.setStatus(SubJobStatus.SUB_JOB_RUNNING);
          jobStore.update(job);

          SubJobResponse response =
              workstationClient.executeSubJob(workstation, toProtoRequest(job, subJob));

          if (response.getStatus() == SubJobStatus.SUB_JOB_DONE) {
            subJob.setResultStates(fromComponentStates(response.getComponentStatesList()));
            subJob.setStatus(SubJobStatus.SUB_JOB_DONE);
          } else {
            log.error(
                "Sub-job {} for job {} failed: {}",
                subJob.getId(),
                jobId,
                response.getErrorMessage());
            subJob.setStatus(SubJobStatus.SUB_JOB_FAILED);
            subJob.setErrorMessage(response.getErrorMessage());
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(response.getErrorMessage());
            jobStore.update(job);
            return;
          }
        } finally {
          workstationRegistry.decrementLoad(workstation.getWorkstationId());
        }
      }

      byte[] merged = resultMerger.merge(job);
      jobStore.setResult(jobId, merged);
      log.info("Job {} completed successfully", jobId);
    } catch (Exception e) {
      log.error("Job {} failed", jobId, e);
      try {
        jobStore.updateStatus(jobId, JobStatus.FAILED, e.getMessage());
      } catch (IOException ioException) {
        log.error("Failed to persist job failure for {}", jobId, ioException);
      }
    }
  }

  private SubJob buildSingleSubJob(Job job, WorkstationInfo workstation) throws IOException {
    List<String> componentLines =
        Files.readAllLines(Path.of(job.getComponentsPath()), StandardCharsets.UTF_8);
    List<String> connectionLines =
        Files.readAllLines(Path.of(job.getConnectionsPath()), StandardCharsets.UTF_8);

    Map<Long, String> componentOwner = new HashMap<>();
    for (String line : componentLines) {
      if (line.isBlank()) {
        continue;
      }
      long componentId = Long.parseLong(line.trim().split("\\s+")[0]);
      componentOwner.put(componentId, workstation.getWorkstationId());
    }

    SubJob subJob = new SubJob();
    subJob.setId(1L);
    subJob.setWorkstationId(workstation.getWorkstationId());
    subJob.setComponentLines(componentLines);
    subJob.setConnectionLines(connectionLines);
    subJob.setComponentOwner(componentOwner);
    return subJob;
  }

  private SubJobRequest toProtoRequest(Job job, SubJob subJob) {
    SubJobRequest.Builder builder =
        SubJobRequest.newBuilder()
            .setJobId(job.getId())
            .setSubjobId(subJob.getId())
            .setSimType(job.getSimType())
            .setEndTime(job.getEndTime())
            .setLocalWorkstationId(subJob.getWorkstationId())
            .addAllComponentLines(subJob.getComponentLines())
            .addAllConnectionLines(subJob.getConnectionLines());
    subJob.getComponentOwner().forEach(builder::putComponentOwner);
    for (WorkstationInfo info : workstationRegistry.getActiveWorkstations()) {
      builder.putWorkstationEndpoints(
          info.getWorkstationId(),
          WorkstationEndpoint.newBuilder()
              .setGrpcHost(info.getGrpcHost())
              .setGrpcPort(info.getGrpcPort())
              .build());
    }
    return builder.build();
  }

  private static List<String[]> fromComponentStates(List<ComponentState> states) {
    List<ComponentState> sorted = new ArrayList<>(states);
    sorted.sort(java.util.Comparator.comparingLong(ComponentState::getComponentId));
    List<String[]> result = new ArrayList<>();
    for (ComponentState state : sorted) {
      result.add(state.getStateValuesList().toArray(new String[0]));
    }
    return result;
  }
}
