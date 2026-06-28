package rs.ac.bg.etf.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.bg.etf.job.Job;
import rs.ac.bg.etf.job.JobStore;
import rs.ac.bg.etf.merge.ResultMerger;
import rs.ac.bg.etf.proto.ComponentState;
import rs.ac.bg.etf.proto.JobStatus;
import rs.ac.bg.etf.proto.RegisterWorkstationRequest;
import rs.ac.bg.etf.proto.SimulationType;
import rs.ac.bg.etf.proto.SubJobRequest;
import rs.ac.bg.etf.proto.SubJobResponse;
import rs.ac.bg.etf.proto.SubJobStatus;
import rs.ac.bg.etf.registry.WorkstationRegistry;

class JobSchedulerTest {

  @Test
  void scheduleRunsSubJobAndMarksJobDone(@TempDir Path tempDir) throws Exception {
    Path miniDir = miniDir();
    Path components = tempDir.resolve("komponente.txt");
    Path connections = tempDir.resolve("veze.txt");
    Files.copy(miniDir.resolve("komponente.txt"), components);
    Files.copy(miniDir.resolve("veze.txt"), connections);

    JobStore jobStore = new JobStore(tempDir.resolve("jobs"));
    WorkstationRegistry registry = new WorkstationRegistry();
    String wsId =
        registry.register(
            RegisterWorkstationRequest.newBuilder()
                .setHostname("test")
                .setGrpcHost("localhost")
                .setGrpcPort(59999)
                .setParallelism(1)
                .build());

    JobScheduler scheduler =
        new JobScheduler(
            jobStore,
            registry,
            (workstation, request) -> buildSuccessResponse(request),
            new ResultMerger(),
            Executors.newSingleThreadExecutor());

    Job job = new Job();
    job.setSimType(SimulationType.OPTIMISTIC);
    job.setEndTime(100L);
    job.setComponentsPath(components.toString());
    job.setConnectionsPath(connections.toString());
    job.setStatus(JobStatus.READY);
    jobStore.create(job);

    scheduler.schedule(job);

    assertTrue(awaitDone(jobStore, job.getId(), 5, TimeUnit.SECONDS));
    Job done = jobStore.get(job.getId()).orElseThrow();
    assertEquals(JobStatus.DONE, done.getStatus());
    assertTrue(done.getResultContent().length > 0);
    assertEquals(wsId, done.getSubJobs().getFirst().getWorkstationId());
  }

  @Test
  void scheduleFailsWhenNoWorkstation(@TempDir Path tempDir) throws Exception {
    JobStore jobStore = new JobStore(tempDir);
    JobScheduler scheduler =
        new JobScheduler(
            jobStore,
            new WorkstationRegistry(),
            (workstation, request) -> SubJobResponse.getDefaultInstance(),
            new ResultMerger(),
            Executors.newSingleThreadExecutor());

    Job job = new Job();
    job.setComponentsPath("c.txt");
    job.setConnectionsPath("v.txt");
    jobStore.create(job);
    scheduler.schedule(job);

    Job failed = jobStore.get(job.getId()).orElseThrow();
    assertEquals(JobStatus.FAILED, failed.getStatus());
    assertEquals("no active workstations", failed.getErrorMessage());
  }

  private static SubJobResponse buildSuccessResponse(SubJobRequest request) {
    SubJobResponse.Builder builder =
        SubJobResponse.newBuilder()
            .setJobId(request.getJobId())
            .setSubjobId(request.getSubjobId())
            .setStatus(SubJobStatus.SUB_JOB_DONE);
    for (String line : request.getComponentLinesList()) {
      if (line.isBlank()) {
        continue;
      }
      builder.addComponentStates(
          ComponentState.newBuilder()
              .setComponentId(Long.parseLong(line.trim().split("\\s+")[0]))
              .addAllStateValues(List.of(line.trim().split("\\s+")))
              .build());
    }
    return builder.build();
  }

  private static boolean awaitDone(JobStore store, long jobId, long timeout, TimeUnit unit)
      throws InterruptedException {
    long deadline = System.nanoTime() + unit.toNanos(timeout);
    while (System.nanoTime() < deadline) {
      Job job = store.get(jobId).orElse(null);
      if (job != null
          && (job.getStatus() == JobStatus.DONE || job.getStatus() == JobStatus.FAILED)) {
        return job.getStatus() == JobStatus.DONE;
      }
      Thread.sleep(50);
    }
    return false;
  }

  private static Path miniDir() throws Exception {
    URL resource = JobSchedulerTest.class.getClassLoader().getResource("mini/komponente.txt");
    if (resource == null) {
      throw new IllegalStateException("mini test resources not found");
    }
    return Path.of(resource.toURI()).getParent();
  }
}
