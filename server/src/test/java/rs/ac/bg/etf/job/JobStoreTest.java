package rs.ac.bg.etf.job;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.bg.etf.proto.JobStatus;
import rs.ac.bg.etf.proto.SimulationType;

class JobStoreTest {

  @Test
  void createAndReloadPreservesJob(@TempDir Path tempDir) throws Exception {
    JobStore store = new JobStore(tempDir);
    Job job = new Job();
    job.setSimType(SimulationType.OPTIMISTIC);
    job.setEndTime(100L);
    job.setComponentsPath("komponente.txt");
    job.setConnectionsPath("veze.txt");
    job.setClientOutputName("out.txt");
    job.setStatus(JobStatus.READY);

    SubJob subJob = new SubJob();
    subJob.setId(1L);
    subJob.setWorkstationId("ws-1");
    subJob.getComponentLines().add("1 Bag");
    job.getSubJobs().add(subJob);

    store.create(job);
    assertEquals(1L, job.getId());

    JobStore reloaded = new JobStore(tempDir);
    reloaded.loadPersistedJobs();
    Job loaded = reloaded.get(1L).orElseThrow();
    assertEquals(JobStatus.READY, loaded.getStatus());
    assertEquals(SimulationType.OPTIMISTIC, loaded.getSimType());
    assertEquals(100L, loaded.getEndTime());
    assertEquals("komponente.txt", loaded.getComponentsPath());
    assertEquals(1, loaded.getSubJobs().size());
    assertEquals("ws-1", loaded.getSubJobs().getFirst().getWorkstationId());
  }

  @Test
  void setResultPersistsDoneJob(@TempDir Path tempDir) throws Exception {
    JobStore store = new JobStore(tempDir);
    Job job = new Job();
    job.setStatus(JobStatus.RUNNING);
    store.create(job);

    byte[] content = "line1\nline2".getBytes();
    store.setResult(job.getId(), content);

    Job loaded = store.get(job.getId()).orElseThrow();
    assertEquals(JobStatus.DONE, loaded.getStatus());
    assertArrayEquals(content, loaded.getResultContent());
    assertTrue(java.nio.file.Files.exists(tempDir.resolve(job.getId() + "/result.txt")));
  }
}
