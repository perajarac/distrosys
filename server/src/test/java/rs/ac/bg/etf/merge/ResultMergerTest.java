package rs.ac.bg.etf.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.job.Job;
import rs.ac.bg.etf.job.SubJob;
import rs.ac.bg.etf.proto.SubJobStatus;

class ResultMergerTest {

  @Test
  void mergeSortsOutputByComponentId() {
    Job job = new Job();
    SubJob subJob = new SubJob();
    subJob.setComponentLines(List.of("2 Collector 2 2 10", "1 Bag 1 2 10", "3 Worker 3"));
    subJob.setResultStates(
        List.of(
            new String[] {"2", "Collector", "2", "2", "10"},
            new String[] {"1", "Bag", "1", "2", "10"},
            new String[] {"3", "Worker", "3"}));
    subJob.setStatus(SubJobStatus.SUB_JOB_DONE);
    job.getSubJobs().add(subJob);

    byte[] merged = new ResultMerger().merge(job);
    String output = new String(merged, StandardCharsets.UTF_8);
    assertEquals("1 Bag 1 2 10\n2 Collector 2 2 10\n3 Worker 3", output);
  }
}
