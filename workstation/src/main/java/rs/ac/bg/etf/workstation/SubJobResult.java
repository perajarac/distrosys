package rs.ac.bg.etf.workstation;

import java.util.List;
import rs.ac.bg.etf.proto.ComponentState;

/** Internal result of a {@link LocalSimulationTask} execution. */
public final class SubJobResult {

  private final boolean success;
  private final List<ComponentState> componentStates;
  private final String errorMessage;

  private SubJobResult(boolean success, List<ComponentState> componentStates, String errorMessage) {
    this.success = success;
    this.componentStates = componentStates;
    this.errorMessage = errorMessage;
  }

  /**
   * @param componentStates sorted component states for the partition
   * @return successful sub-job result
   */
  public static SubJobResult success(List<ComponentState> componentStates) {
    return new SubJobResult(true, List.copyOf(componentStates), null);
  }

  /**
   * @param errorMessage failure description
   * @return failed sub-job result
   */
  public static SubJobResult failure(String errorMessage) {
    return new SubJobResult(false, List.of(), errorMessage);
  }

  /**
   * @return {@code true} when simulation completed without error
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * @return component states sorted by component id
   */
  public List<ComponentState> getComponentStates() {
    return componentStates;
  }

  /**
   * @return error description when not successful
   */
  public String getErrorMessage() {
    return errorMessage;
  }
}
