package rs.ac.bg.etf.sleep.simulation;

import java.io.*;

/**
 * Optimistic {@link Simulator} that rolls back all components on out-of-order events.
 *
 * <p>When an event with logical time earlier than the current {@link #lTime} is dequeued, {@link
 * #restart(long)} is called on every component and processing stops for that step.
 *
 * @param <T> payload type of events in this simulation (must be {@link Serializable})
 */
public class SimulatorOptimistic<T extends Serializable> extends Simulator<T> {

  /**
   * Creates an optimistic rollback simulator.
   *
   * @param id simulator identifier
   */
  public SimulatorOptimistic(int id) {
    super(id);
  }

  /**
   * Dequeues the next event, rolling back on causality violation or dispatching if valid.
   *
   * <p>If {@link #lTime} exceeds the event's logical time, all components are restarted to that
   * time. Otherwise logical time is advanced and valid events are processed.
   */
  @Override
  public void execute() {
    Event<T> m = queue.getEvent();
    if (lTime > m.lTime) {
      restart(m.lTime);
      return;
    }
    lastEvent = m;
    lTime = lastEvent.lTime;
    if (lastEvent.ok()) {
      work(lastEvent);
      pastEvents(lastEvent);
    }
  }

  /**
   * Restarts every component in the netlist to the given logical time.
   *
   * @param endTime logical time to roll back to
   */
  public void restart(long endTime) {
    // queue.cancel(endTime);
    for (Long l : netlist.getComponents().keySet()) {
      netlist.getComponents().get(l).restart(endTime);
    }
  }
}
