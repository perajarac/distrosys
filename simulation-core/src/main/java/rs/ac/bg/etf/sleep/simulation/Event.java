package rs.ac.bg.etf.sleep.simulation;

import java.io.*;

/**
 * Serializable discrete-event message routed between simulation components.
 *
 * <p>Events are ordered by logical time ({@link #getlTime()}), then source and destination
 * identifiers, for use in priority queues.
 *
 * @param <V> payload type carried in {@link #getData()}
 */
public class Event<V> implements Comparable<Event<V>>, Serializable {
  private static final long serialVersionUID = 1L;

  /** Status indicating the event is valid and should be processed. */
  public static final int OK = 0;

  /** Status indicating the event is invalid or cancelled. */
  public static final int NOK = 1;

  /** Monotonically increasing counter used to assign unique event identifiers. */
  public static long ID = 0;

  /** Unique identifier of this event. */
  long id;

  /** Processing status ({@link #OK} or {@link #NOK}). */
  int status;

  /** Logical time at which this event was created. */
  long lTimeCreated;

  /** Logical time at which this event should be processed. */
  long lTime;

  /** Identifier of the source component. */
  long srcID;

  /** Output port on the source component. */
  int srcPort;

  /** Identifier of the destination component. */
  long dstID;

  /** Input port on the destination component. */
  int dstPort;

  /** Payload data attached to this event. */
  V data;

  /**
   * Creates an event with the given routing and timing information.
   *
   * <p>Status is initialized to {@link #OK} and a unique id is assigned from {@link #ID}.
   *
   * @param lTimeCreated logical creation time
   * @param lTime logical processing time
   * @param srcID source component identifier
   * @param srcPort source port number
   * @param dstID destination component identifier
   * @param dstPort destination port number
   */
  public Event(long lTimeCreated, long lTime, long srcID, int srcPort, long dstID, int dstPort) {
    this.lTimeCreated = lTimeCreated;
    this.lTime = lTime;
    this.srcID = srcID;
    this.srcPort = srcPort;
    this.dstID = dstID;
    this.dstPort = dstPort;
    this.status = OK;
    this.id = ID++;
  }

  /** Creates a default event with zeroed routing and timing fields. */
  public Event() {
    this(0, 0, 0, 0, 0, 0);
  }

  /**
   * Compares this event to another for priority-queue ordering.
   *
   * <p>Orders by {@link #lTime}, then {@link #srcID}, then {@link #dstID}, then payload equality.
   *
   * @param e event to compare against
   * @return negative, zero, or positive as this event orders before, equal to, or after {@code e};
   *     returns {@code -1} if {@code e} is {@code null}
   */
  public int compareTo(Event<V> e) {
    if (e == null) return -1;
    int result =
        lTime < e.lTime
            ? -1
            : lTime > e.lTime
                ? 1
                : srcID == dstID
                    ? -1
                    : srcID < e.srcID
                        ? -1
                        : srcID > e.srcID
                            ? 1
                            : dstID < e.dstID
                                ? -1
                                : dstID > e.dstID
                                    ? 1
                                    : data != null && data.equals(e.data) ? 0 : -1;
    return result;
  }

  /**
   * Returns whether this event has status {@link #OK}.
   *
   * @return {@code true} if the event is valid
   */
  public boolean ok() {
    return status == OK;
  }

  /**
   * Returns a shallow copy of this event (same payload reference).
   *
   * @return a new event with identical routing and timing fields
   */
  public Event<V> copy() {
    Event<V> result = new Event<V>();
    result.srcID = srcID;
    result.srcPort = srcPort;
    result.lTime = lTime;
    result.status = status;
    result.dstID = dstID;
    result.dstPort = dstPort;

    result.lTimeCreated = lTimeCreated;
    result.id = id;
    result.data = data;

    return result;
  }

  /**
   * Returns a string representation of this event's fields.
   *
   * @return string containing routing, timing, status, id, and data
   */
  public String toString() {
    String result =
        ""
            + srcID
            + ", "
            + srcPort
            + ", "
            + lTime
            + ", "
            + status
            + ", "
            + dstID
            + ", "
            + dstPort
            + ", "
            + lTimeCreated
            + ", "
            + id
            + ", "
            + data.toString();
    return result;
  }

  /**
   * Returns the unique event identifier.
   *
   * @return event id
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the unique event identifier.
   *
   * @param id event id
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the processing status.
   *
   * @return {@link #OK} or {@link #NOK}
   */
  public int getStatus() {
    return status;
  }

  /**
   * Sets the processing status.
   *
   * @param status {@link #OK} or {@link #NOK}
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * Returns the logical creation time.
   *
   * @return creation time
   */
  public long getlTimeCreated() {
    return lTimeCreated;
  }

  /**
   * Sets the logical creation time.
   *
   * @param lTimeCreated creation time
   */
  public void setlTimeCreated(long lTimeCreated) {
    this.lTimeCreated = lTimeCreated;
  }

  /**
   * Returns the logical processing time.
   *
   * @return processing time
   */
  public long getlTime() {
    return lTime;
  }

  /**
   * Sets the logical processing time.
   *
   * @param lTime processing time
   */
  public void setlTime(long lTime) {
    this.lTime = lTime;
  }

  /**
   * Returns the source component identifier.
   *
   * @return source component id
   */
  public long getSrcID() {
    return srcID;
  }

  /**
   * Sets the source component identifier.
   *
   * @param srcID source component id
   */
  public void setSrcID(long srcID) {
    this.srcID = srcID;
  }

  /**
   * Returns the source port number.
   *
   * @return source port
   */
  public int getSrcPort() {
    return srcPort;
  }

  /**
   * Sets the source port number.
   *
   * @param srcPort source port
   */
  public void setSrcPort(int srcPort) {
    this.srcPort = srcPort;
  }

  /**
   * Returns the destination component identifier.
   *
   * @return destination component id
   */
  public long getDstID() {
    return dstID;
  }

  /**
   * Sets the destination component identifier.
   *
   * @param dstID destination component id
   */
  public void setDstID(long dstID) {
    this.dstID = dstID;
  }

  /**
   * Returns the destination port number.
   *
   * @return destination port
   */
  public int getDstPort() {
    return dstPort;
  }

  /**
   * Sets the destination port number.
   *
   * @param dstPort destination port
   */
  public void setDstPort(int dstPort) {
    this.dstPort = dstPort;
  }

  /**
   * Returns the event payload.
   *
   * @return payload data, or {@code null} if none
   */
  public V getData() {
    return data;
  }

  /**
   * Sets the event payload.
   *
   * @param data payload data
   */
  public void setData(V data) {
    this.data = data;
  }
}
