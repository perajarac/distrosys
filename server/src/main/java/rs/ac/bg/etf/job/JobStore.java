package rs.ac.bg.etf.job;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.bg.etf.proto.JobStatus;
import rs.ac.bg.etf.proto.SimulationType;
import rs.ac.bg.etf.proto.SubJobStatus;

/** In-memory job registry with write-through JSON persistence for client reconnect. */
public class JobStore {

  private static final Logger log = LoggerFactory.getLogger(JobStore.class);

  private final Path jobsDir;
  private final ConcurrentHashMap<Long, Job> jobs = new ConcurrentHashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);

  /**
   * @param jobsDir directory where per-job JSON files are stored
   */
  public JobStore(Path jobsDir) {
    this.jobsDir = jobsDir;
  }

  /**
   * Loads persisted jobs from disk into memory.
   *
   * @throws IOException if the jobs directory cannot be read
   */
  public void loadPersistedJobs() throws IOException {
    Files.createDirectories(jobsDir);
    try (Stream<Path> paths = Files.list(jobsDir)) {
      paths.filter(p -> p.getFileName().toString().endsWith(".json")).forEach(this::loadJobFile);
    }
    jobs.keySet().stream().max(Long::compareTo).ifPresent(maxId -> nextId.set(maxId + 1));
  }

  /**
   * @return directory used for job persistence
   */
  public Path getJobsDir() {
    return jobsDir;
  }

  /**
   * Creates a new job, assigns an id, and persists it.
   *
   * @param job job without id (id is assigned)
   * @return persisted job with id set
   * @throws IOException if persistence fails
   */
  public Job create(Job job) throws IOException {
    job.setId(nextId.getAndIncrement());
    jobs.put(job.getId(), job);
    persist(job);
    return job;
  }

  /**
   * @param jobId job identifier
   * @return job if present
   */
  public Optional<Job> get(long jobId) {
    return Optional.ofNullable(jobs.get(jobId));
  }

  /**
   * @return snapshot of all known jobs
   */
  public Collection<Job> listAll() {
    return List.copyOf(jobs.values());
  }

  /**
   * Updates job status and persists the change.
   *
   * @param jobId job identifier
   * @param status new status
   * @param message optional status or error message
   * @throws IOException if persistence fails
   */
  public void updateStatus(long jobId, JobStatus status, String message) throws IOException {
    Job job = requireJob(jobId);
    job.setStatus(status);
    if (message != null) {
      job.setErrorMessage(message);
    }
    if (JobProtoMapper.isTerminal(status)) {
      job.setFinishedAt(Instant.now());
    }
    persist(job);
  }

  /**
   * Replaces the full job record and persists it.
   *
   * @param job updated job
   * @throws IOException if persistence fails
   */
  public void update(Job job) throws IOException {
    jobs.put(job.getId(), job);
    persist(job);
  }

  /**
   * Stores merged result content and marks the job done.
   *
   * @param jobId job identifier
   * @param resultContent merged output bytes
   * @throws IOException if persistence fails
   */
  public void setResult(long jobId, byte[] resultContent) throws IOException {
    Job job = requireJob(jobId);
    job.setResultContent(resultContent);
    job.setStatus(JobStatus.DONE);
    job.setFinishedAt(Instant.now());
    persist(job);
    Path resultPath = jobsDir.resolve(jobId + "/result.txt");
    Files.createDirectories(resultPath.getParent());
    Files.write(resultPath, resultContent);
  }

  private Job requireJob(long jobId) {
    Job job = jobs.get(jobId);
    if (job == null) {
      throw new IllegalArgumentException("Unknown job id: " + jobId);
    }
    return job;
  }

  private void loadJobFile(Path path) {
    try {
      String json = Files.readString(path, StandardCharsets.UTF_8);
      Job job = fromJson(json);
      jobs.put(job.getId(), job);
    } catch (IOException e) {
      log.error("Failed to load job file {}", path, e);
    }
  }

  private void persist(Job job) throws IOException {
    Files.createDirectories(jobsDir);
    Path file = jobsDir.resolve(job.getId() + ".json");
    Files.writeString(file, toJson(job), StandardCharsets.UTF_8);
  }

  static String toJson(Job job) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    appendField(sb, "id", Long.toString(job.getId()));
    appendField(sb, "status", job.getStatus().name());
    appendField(
        sb,
        "simType",
        job.getSimType() != null ? job.getSimType().name() : "SIMULATION_TYPE_UNSPECIFIED");
    appendField(sb, "endTime", Long.toString(job.getEndTime()));
    appendField(sb, "componentsPath", escape(job.getComponentsPath()));
    appendField(sb, "connectionsPath", escape(job.getConnectionsPath()));
    appendField(sb, "clientOutputName", escape(job.getClientOutputName()));
    appendField(
        sb, "createdAt", escape(job.getCreatedAt() != null ? job.getCreatedAt().toString() : ""));
    appendField(
        sb,
        "finishedAt",
        escape(job.getFinishedAt() != null ? job.getFinishedAt().toString() : ""));
    appendField(
        sb, "errorMessage", escape(job.getErrorMessage() != null ? job.getErrorMessage() : ""));
    if (job.getResultContent() != null) {
      appendField(
          sb, "resultContent", escape(Base64.getEncoder().encodeToString(job.getResultContent())));
    } else {
      appendField(sb, "resultContent", "");
    }
    sb.append("  \"subJobs\": [\n");
    for (int i = 0; i < job.getSubJobs().size(); i++) {
      sb.append(toJson(job.getSubJobs().get(i)));
      if (i < job.getSubJobs().size() - 1) {
        sb.append(',');
      }
      sb.append('\n');
    }
    sb.append("  ]\n}");
    return sb.toString();
  }

  private static String toJson(SubJob subJob) {
    StringBuilder sb = new StringBuilder();
    sb.append("    {\n");
    appendField(sb, "id", Long.toString(subJob.getId()));
    appendField(sb, "workstationId", escape(subJob.getWorkstationId()));
    appendField(sb, "status", subJob.getStatus().name());
    appendField(
        sb,
        "errorMessage",
        escape(subJob.getErrorMessage() != null ? subJob.getErrorMessage() : ""));
    sb.append("      \"componentLines\": ")
        .append(stringListJson(subJob.getComponentLines()))
        .append(",\n");
    sb.append("      \"connectionLines\": ")
        .append(stringListJson(subJob.getConnectionLines()))
        .append(",\n");
    sb.append("      \"componentOwner\": ")
        .append(mapJson(subJob.getComponentOwner()))
        .append(",\n");
    sb.append("      \"resultStates\": ")
        .append(resultStatesJson(subJob.getResultStates()))
        .append("\n");
    sb.append("    }");
    return sb.toString();
  }

  static Job fromJson(String json) {
    Job job = new Job();
    job.setId(Long.parseLong(extractString(json, "id")));
    job.setStatus(JobStatus.valueOf(extractString(json, "status")));
    job.setSimType(SimulationType.valueOf(extractString(json, "simType")));
    if (job.getSimType() == SimulationType.SIMULATION_TYPE_UNSPECIFIED) {
      job.setSimType(null);
    }
    job.setEndTime(Long.parseLong(extractString(json, "endTime")));
    job.setComponentsPath(unescape(extractString(json, "componentsPath")));
    job.setConnectionsPath(unescape(extractString(json, "connectionsPath")));
    job.setClientOutputName(unescape(extractString(json, "clientOutputName")));
    String createdAt = unescape(extractString(json, "createdAt"));
    if (!createdAt.isEmpty()) {
      job.setCreatedAt(Instant.parse(createdAt));
    }
    String finishedAt = unescape(extractString(json, "finishedAt"));
    if (!finishedAt.isEmpty()) {
      job.setFinishedAt(Instant.parse(finishedAt));
    }
    job.setErrorMessage(unescape(extractString(json, "errorMessage")));
    String resultB64 = unescape(extractString(json, "resultContent"));
    if (!resultB64.isEmpty()) {
      job.setResultContent(Base64.getDecoder().decode(resultB64));
    }
    job.setSubJobs(parseSubJobs(extractArray(json, "subJobs")));
    return job;
  }

  private static List<SubJob> parseSubJobs(String arrayBody) {
    List<SubJob> subJobs = new ArrayList<>();
    if (arrayBody == null || arrayBody.isBlank()) {
      return subJobs;
    }
    List<String> objects = splitTopLevelObjects(arrayBody);
    for (String obj : objects) {
      SubJob subJob = new SubJob();
      subJob.setId(Long.parseLong(extractString(obj, "id")));
      subJob.setWorkstationId(unescape(extractString(obj, "workstationId")));
      subJob.setStatus(SubJobStatus.valueOf(extractString(obj, "status")));
      subJob.setErrorMessage(unescape(extractString(obj, "errorMessage")));
      subJob.setComponentLines(parseStringList(extractArray(obj, "componentLines")));
      subJob.setConnectionLines(parseStringList(extractArray(obj, "connectionLines")));
      subJob.setComponentOwner(parseLongStringMap(extractArray(obj, "componentOwner")));
      subJob.setResultStates(parseResultStates(extractArray(obj, "resultStates")));
      subJobs.add(subJob);
    }
    return subJobs;
  }

  private static void appendField(StringBuilder sb, String name, String rawValue) {
    sb.append("  \"").append(name).append("\": ");
    if (name.equals("id") || name.equals("endTime")) {
      sb.append(rawValue);
    } else {
      sb.append('"').append(escape(rawValue)).append('"');
    }
    sb.append(",\n");
  }

  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }

  private static String unescape(String value) {
    if (value == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '\\' && i + 1 < value.length()) {
        char next = value.charAt(++i);
        switch (next) {
          case '\\' -> sb.append('\\');
          case '"' -> sb.append('"');
          case 'n' -> sb.append('\n');
          case 'r' -> sb.append('\r');
          default -> sb.append(next);
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String stringListJson(List<String> values) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < values.size(); i++) {
      sb.append('"').append(escape(values.get(i))).append('"');
      if (i < values.size() - 1) {
        sb.append(',');
      }
    }
    sb.append(']');
    return sb.toString();
  }

  private static String mapJson(Map<Long, String> map) {
    StringBuilder sb = new StringBuilder("{");
    int i = 0;
    for (Map.Entry<Long, String> entry : map.entrySet()) {
      sb.append('"')
          .append(entry.getKey())
          .append("\":\"")
          .append(escape(entry.getValue()))
          .append('"');
      if (++i < map.size()) {
        sb.append(',');
      }
    }
    sb.append('}');
    return sb.toString();
  }

  private static String resultStatesJson(List<String[]> states) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < states.size(); i++) {
      sb.append('[');
      String[] row = states.get(i);
      for (int j = 0; j < row.length; j++) {
        sb.append('"').append(escape(row[j])).append('"');
        if (j < row.length - 1) {
          sb.append(',');
        }
      }
      sb.append(']');
      if (i < states.size() - 1) {
        sb.append(',');
      }
    }
    sb.append(']');
    return sb.toString();
  }

  private static String extractString(String json, String field) {
    String key = "\"" + field + "\":";
    int start = json.indexOf(key);
    if (start < 0) {
      throw new IllegalArgumentException("Missing field: " + field);
    }
    start += key.length();
    while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
      start++;
    }
    if (start < json.length() && json.charAt(start) == '"') {
      start++;
      StringBuilder sb = new StringBuilder();
      for (int i = start; i < json.length(); i++) {
        char c = json.charAt(i);
        if (c == '\\') {
          sb.append(c);
          if (i + 1 < json.length()) {
            sb.append(json.charAt(++i));
          }
        } else if (c == '"') {
          return sb.toString();
        } else {
          sb.append(c);
        }
      }
      throw new IllegalArgumentException("Unterminated string for field: " + field);
    }
    int end = start;
    while (end < json.length()
        && json.charAt(end) != ','
        && json.charAt(end) != '\n'
        && json.charAt(end) != '}') {
      end++;
    }
    return json.substring(start, end).trim();
  }

  private static String extractArray(String json, String field) {
    String key = "\"" + field + "\":";
    int start = json.indexOf(key);
    if (start < 0) {
      return "";
    }
    start = json.indexOf('[', start);
    if (start < 0) {
      return "";
    }
    int depth = 0;
    for (int i = start; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == '[') {
        depth++;
      } else if (c == ']') {
        depth--;
        if (depth == 0) {
          return json.substring(start + 1, i);
        }
      }
    }
    return "";
  }

  private static List<String> splitTopLevelObjects(String body) {
    List<String> objects = new ArrayList<>();
    int depth = 0;
    int start = -1;
    for (int i = 0; i < body.length(); i++) {
      char c = body.charAt(i);
      if (c == '{') {
        if (depth == 0) {
          start = i;
        }
        depth++;
      } else if (c == '}') {
        depth--;
        if (depth == 0 && start >= 0) {
          objects.add(body.substring(start, i + 1));
          start = -1;
        }
      }
    }
    return objects;
  }

  private static List<String> parseStringList(String body) {
    List<String> values = new ArrayList<>();
    if (body == null || body.isBlank()) {
      return values;
    }
    int i = 0;
    while (i < body.length()) {
      while (i < body.length() && body.charAt(i) != '"') {
        i++;
      }
      if (i >= body.length()) {
        break;
      }
      i++;
      StringBuilder sb = new StringBuilder();
      while (i < body.length()) {
        char c = body.charAt(i);
        if (c == '\\') {
          sb.append(c);
          if (i + 1 < body.length()) {
            sb.append(body.charAt(++i));
          }
          i++;
        } else if (c == '"') {
          i++;
          break;
        } else {
          sb.append(c);
          i++;
        }
      }
      values.add(unescape(sb.toString()));
    }
    return values;
  }

  private static Map<Long, String> parseLongStringMap(String body) {
    Map<Long, String> map = new java.util.HashMap<>();
    if (body == null || body.isBlank()) {
      return map;
    }
    int i = 0;
    while (i < body.length()) {
      while (i < body.length() && body.charAt(i) != '"') {
        i++;
      }
      if (i >= body.length()) {
        break;
      }
      i++;
      StringBuilder key = new StringBuilder();
      while (i < body.length() && body.charAt(i) != '"') {
        key.append(body.charAt(i++));
      }
      i++;
      while (i < body.length() && body.charAt(i) != '"') {
        i++;
      }
      i++;
      StringBuilder value = new StringBuilder();
      while (i < body.length()) {
        char c = body.charAt(i);
        if (c == '\\') {
          value.append(c);
          if (i + 1 < body.length()) {
            value.append(body.charAt(++i));
          }
          i++;
        } else if (c == '"') {
          i++;
          break;
        } else {
          value.append(c);
          i++;
        }
      }
      map.put(Long.parseLong(key.toString()), unescape(value.toString()));
    }
    return map;
  }

  private static List<String[]> parseResultStates(String body) {
    List<String[]> states = new ArrayList<>();
    if (body == null || body.isBlank()) {
      return states;
    }
    int i = 0;
    while (i < body.length()) {
      while (i < body.length() && body.charAt(i) != '[') {
        i++;
      }
      if (i >= body.length()) {
        break;
      }
      int depth = 0;
      int start = i;
      for (; i < body.length(); i++) {
        char c = body.charAt(i);
        if (c == '[') {
          depth++;
        } else if (c == ']') {
          depth--;
          if (depth == 0) {
            String inner = body.substring(start + 1, i);
            states.add(parseStringList(inner).toArray(new String[0]));
            i++;
            break;
          }
        }
      }
    }
    return states;
  }
}
