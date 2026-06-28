package rs.ac.bg.etf.sleep.simulation;

import java.util.*;

/**
 * Abstraction over the discrete-event priority queue used by a {@link Simulator}.
 *
 * @param <V> payload type of events stored in this buffer
 */
public interface SimBuffer<V> {

  /**
   * Enqueues a single event.
   *
   * @param event event to add
   */
  public void putEvent(Event<V> event);

  /**
   * Enqueues multiple events.
   *
   * @param events events to add
   */
  public void putEvents(List<Event<V>> events);

  /**
   * Removes and returns the highest-priority event from the queue.
   *
   * @return the next event to process
   */
  public Event<V> getEvent();

  /**
   * Removes and returns the next event wrapped in a single-element list.
   *
   * @return list containing one event
   */
  public List<Event<V>> getEvents();

  /**
   * Returns whether the queue contains no events.
   *
   * @return {@code true} if the queue is empty
   */
  public boolean isEmpty();

  /**
   * Returns the minimum rank (priority key) of queued events.
   *
   * @return minimum rank, or implementation-defined value when empty
   */
  public long getMinrank();
}
