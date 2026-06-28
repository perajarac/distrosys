package rs.ac.bg.etf.merge;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import rs.ac.bg.etf.job.Job;
import rs.ac.bg.etf.job.SubJob;

/** Merges sub-job component states into a single deterministic output file body. */
public class ResultMerger {

  /**
   * Builds merged UTF-8 output bytes from completed sub-job results.
   *
   * @param job job with sub-jobs containing result states
   * @return one line per component (sorted by component id), values space-separated
   * @throws IllegalStateException if expected component states are missing
   */
  public byte[] merge(Job job) {
    Map<Long, String[]> statesById = new TreeMap<>();
    for (SubJob subJob : job.getSubJobs()) {
      for (String[] state : subJob.getResultStates()) {
        if (state.length == 0) {
          continue;
        }
        statesById.put(Long.parseLong(state[0]), state);
      }
    }

    List<Long> expectedIds = parseComponentIds(job);
    for (Long expectedId : expectedIds) {
      if (!statesById.containsKey(expectedId)) {
        throw new IllegalStateException("Missing result state for component id " + expectedId);
      }
    }

    StringBuilder output = new StringBuilder();
    for (Long componentId : expectedIds) {
      if (output.length() > 0) {
        output.append('\n');
      }
      output.append(formatStateLine(statesById.get(componentId)));
    }
    return output.toString().getBytes(StandardCharsets.UTF_8);
  }

  private static List<Long> parseComponentIds(Job job) {
    List<Long> ids = new ArrayList<>();
    for (SubJob subJob : job.getSubJobs()) {
      for (String line : subJob.getComponentLines()) {
        if (line.isBlank()) {
          continue;
        }
        ids.add(Long.parseLong(line.trim().split("\\s+")[0]));
      }
    }
    ids.sort(Long::compareTo);
    return ids;
  }

  private static String formatStateLine(String[] state) {
    StringBuilder line = new StringBuilder();
    for (int i = 0; i < state.length; i++) {
      if (i > 0) {
        line.append(' ');
      }
      line.append(state[i]);
    }
    return line.toString();
  }
}
