package rs.ac.bg.etf.workstation;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.bg.etf.kdp.simulation.components.Field;
import rs.ac.bg.etf.sleep.simulation.Event;
import rs.ac.bg.etf.sleep.simulation.SimBuffer;
import rs.ac.bg.etf.sleep.simulation.SimBufferLocal;

/** Distributed {@link SimBuffer} that routes events locally or to peer workstations. */
public class SimBufferNetwork implements SimBuffer<Field> {

  private static final Logger log = LoggerFactory.getLogger(SimBufferNetwork.class);

  private final long jobId;
  private final String localWorkstationId;
  private final Map<Long, String> componentOwner;
  private final WorkstationPeerRegistry peers;
  private final SimBufferLocal<Field> localQueue;

  /**
   * @param jobId parent job identifier
   * @param localWorkstationId this workstation's id
   * @param componentOwner global component id to workstation id map
   * @param peers peer gRPC clients
   * @param localQueue local priority queue consumed by the simulator
   */
  public SimBufferNetwork(
      long jobId,
      String localWorkstationId,
      Map<Long, String> componentOwner,
      WorkstationPeerRegistry peers,
      SimBufferLocal<Field> localQueue) {
    this.jobId = jobId;
    this.localWorkstationId = localWorkstationId;
    this.componentOwner = componentOwner;
    this.peers = peers;
    this.localQueue = localQueue;
  }

  @Override
  public void putEvent(Event<Field> event) {
    String targetWs = componentOwner.get(event.getDstID());
    if (targetWs == null || targetWs.equals(localWorkstationId)) {
      localQueue.putEvent(event);
    } else {
      log.debug(
          "Forwarding event for job {} to workstation {} (dst component {})",
          jobId,
          targetWs,
          event.getDstID());
      peers.forwardEvent(targetWs, jobId, event);
    }
  }

  @Override
  public void putEvents(List<Event<Field>> events) {
    for (Event<Field> event : events) {
      putEvent(event);
    }
  }

  /**
   * Enqueues an event received from a remote workstation via {@code ForwardEvent}.
   *
   * @param event remote event destined for a local component
   */
  public void deliverRemoteEvent(Event<Field> event) {
    localQueue.putEvent(event);
  }

  @Override
  public Event<Field> getEvent() {
    return localQueue.getEvent();
  }

  @Override
  public List<Event<Field>> getEvents() {
    return localQueue.getEvents();
  }

  @Override
  public boolean isEmpty() {
    return localQueue.isEmpty();
  }

  @Override
  public long getMinrank() {
    return localQueue.getMinrank();
  }
}
