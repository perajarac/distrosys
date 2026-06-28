package rs.ac.bg.etf.sleep.simulation;

import java.util.*;

/**
 * Contract for a discrete-event simulation component.
 *
 * <p>Components are registered in a {@link Netlist}, initialized via {@link #init()}, and driven by
 * incoming {@link Event} messages. State can be serialized for checkpointing and restored via
 * {@link #restart(long)} during optimistic rollback.
 *
 * @param <V> payload type carried by events processed by this component
 */
public interface SimComponent<V> {

  /**
   * Processes one incoming event and returns zero or more outgoing events.
   *
   * @param msg the event to handle
   * @return list of events to enqueue (may be empty)
   */
  public List<Event<V>> execute(Event<V> msg);

  /**
   * Bootstraps the component and returns initial events to seed the simulation queue.
   *
   * @return list of initial events (may be empty)
   */
  public List<Event<V>> init();

  /**
   * Serializes the component state to a string array.
   *
   * @return string representation of component state
   */
  public String[] getState();

  /**
   * Restores component state from a string array produced by {@link #getState()}.
   *
   * @param args serialized state fields
   */
  public void setState(String[] args);

  /**
   * Rolls the component back to a prior logical time during optimistic simulation.
   *
   * @param time logical time to restart from
   */
  public void restart(long time);
}
