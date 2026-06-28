package rs.ac.bg.etf.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rs.ac.bg.etf.proto.SubJobStatus;

/** A partition of a {@link Job} assigned to a single workstation for execution. */
public class SubJob {

  private long id;
  private String workstationId;
  private SubJobStatus status;
  private List<String> componentLines;
  private List<String> connectionLines;
  private Map<Long, String> componentOwner;
  private List<String[]> resultStates;
  private String errorMessage;

  /** Creates an empty sub-job in {@link SubJobStatus#SUB_JOB_PENDING} state. */
  public SubJob() {
    this.status = SubJobStatus.SUB_JOB_PENDING;
    this.componentLines = new ArrayList<>();
    this.connectionLines = new ArrayList<>();
    this.componentOwner = new HashMap<>();
    this.resultStates = new ArrayList<>();
  }

  /**
   * @return sub-job identifier unique within the parent job
   */
  public long getId() {
    return id;
  }

  /**
   * @param id sub-job identifier unique within the parent job
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return assigned workstation identifier
   */
  public String getWorkstationId() {
    return workstationId;
  }

  /**
   * @param workstationId assigned workstation identifier
   */
  public void setWorkstationId(String workstationId) {
    this.workstationId = workstationId;
  }

  /**
   * @return current sub-job execution status
   */
  public SubJobStatus getStatus() {
    return status;
  }

  /**
   * @param status current sub-job execution status
   */
  public void setStatus(SubJobStatus status) {
    this.status = status;
  }

  /**
   * @return raw component file lines for this partition
   */
  public List<String> getComponentLines() {
    return componentLines;
  }

  /**
   * @param componentLines raw component file lines for this partition
   */
  public void setComponentLines(List<String> componentLines) {
    this.componentLines = componentLines;
  }

  /**
   * @return raw connection file lines (including header) for this partition
   */
  public List<String> getConnectionLines() {
    return connectionLines;
  }

  /**
   * @param connectionLines raw connection file lines (including header) for this partition
   */
  public void setConnectionLines(List<String> connectionLines) {
    this.connectionLines = connectionLines;
  }

  /**
   * @return global map of component id to owning workstation id
   */
  public Map<Long, String> getComponentOwner() {
    return componentOwner;
  }

  /**
   * @param componentOwner global map of component id to owning workstation id
   */
  public void setComponentOwner(Map<Long, String> componentOwner) {
    this.componentOwner = componentOwner;
  }

  /**
   * @return per-component state rows returned by the workstation
   */
  public List<String[]> getResultStates() {
    return resultStates;
  }

  /**
   * @param resultStates per-component state rows returned by the workstation
   */
  public void setResultStates(List<String[]> resultStates) {
    this.resultStates = resultStates;
  }

  /**
   * @return error description when status is failed
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @param errorMessage error description when status is failed
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
