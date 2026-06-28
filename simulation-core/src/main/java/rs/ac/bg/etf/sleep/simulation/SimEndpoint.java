package rs.ac.bg.etf.sleep.simulation;

import java.io.*;

/**
 * Serializable pair identifying a component port in the simulation netlist.
 *
 * <p>Used as a key when wiring connections in {@link Netlist} and when routing events between
 * components.
 */
public class SimEndpoint implements Serializable, Comparable<SimEndpoint> {
  private static final long serialVersionUID = 1L;

  /** Sentinel value indicating an unknown component identifier. */
  public static final long HOSTUNKNOWN = -1;

  /** Sentinel value indicating an unknown port number. */
  public static final int PORTUNKNOWN = -1;

  /** Identifier of the component owning this endpoint. */
  public long componentID;

  /** Port number on the component. */
  public int componentPort;

  /**
   * Creates an endpoint for the given component and port.
   *
   * @param id component identifier
   * @param port port number on the component
   */
  public SimEndpoint(long id, int port) {
    componentID = id;
    componentPort = port;
  }

  /**
   * Creates an endpoint with unknown component and port ({@link #HOSTUNKNOWN}, {@link
   * #PORTUNKNOWN}).
   */
  public SimEndpoint() {
    this(SimEndpoint.HOSTUNKNOWN, SimEndpoint.PORTUNKNOWN);
  }

  /**
   * Returns the component identifier.
   *
   * @return component identifier
   */
  public long getComponentID() {
    return componentID;
  }

  /**
   * Sets the component identifier.
   *
   * @param componentID component identifier
   */
  public void setComponentID(long componentID) {
    this.componentID = componentID;
  }

  /**
   * Returns the port number.
   *
   * @return port number on the component
   */
  public int getComponentPort() {
    return componentPort;
  }

  /**
   * Sets the port number.
   *
   * @param componentPort port number on the component
   */
  public void setComponentPort(int componentPort) {
    this.componentPort = componentPort;
  }

  /**
   * Compares this endpoint to another object for equality.
   *
   * @param o object to compare
   * @return {@code true} if {@code o} is a {@link SimEndpoint} with the same component ID and port
   */
  public boolean equals(Object o) {
    if (o == null || !(o instanceof SimEndpoint)) return false;
    else
      return (componentID == ((SimEndpoint) o).getComponentID())
          && (componentPort == ((SimEndpoint) o).getComponentPort());
  }

  /**
   * Returns a hash code based on component ID and port.
   *
   * @return hash code for use in hash-based collections
   */
  public int hashCode() {
    return (int) (componentID * 517 + componentPort);
  }

  /**
   * Returns a string representation of this endpoint.
   *
   * @return string in the form {@code "componentID, componentPort"}
   */
  public String toString() {
    String result = "";
    result += componentID + ", " + componentPort;
    return result;
  }

  /**
   * Compares this endpoint to another by component ID, then by port.
   *
   * @param arg endpoint to compare against
   * @return negative, zero, or positive as this endpoint orders before, equal to, or after {@code
   *     arg}
   */
  public int compareTo(SimEndpoint arg) {
    return componentID > arg.componentID
        ? +1
        : componentID < arg.componentID
            ? -1
            : componentPort > arg.componentPort ? +1 : componentPort < arg.componentPort ? -1 : 0;
  }
}
