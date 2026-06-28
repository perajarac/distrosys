package rs.ac.bg.etf.workstation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.kdp.simulation.components.Field;
import rs.ac.bg.etf.sleep.simulation.Event;
import rs.ac.bg.etf.sleep.simulation.SimBufferLocal;

/** Unit tests for {@link SimBufferNetwork} routing behavior. */
class SimBufferNetworkTest {

  private SimBufferLocal<Field> localQueue;
  private RecordingPeerRegistry peers;
  private SimBufferNetwork network;

  @BeforeEach
  void setUp() {
    localQueue = new SimBufferLocal<>();
    peers = new RecordingPeerRegistry();
    Map<Long, String> owners = new HashMap<>();
    owners.put(1L, "ws-local");
    owners.put(2L, "ws-remote");
    network = new SimBufferNetwork(42L, "ws-local", owners, peers, localQueue);
  }

  @AfterEach
  void tearDown() {
    peers.shutdown();
  }

  @Test
  void localDestinationEnqueuesLocally() {
    Event<Field> event = new Event<>(0, 1, 1, 1, 1, 1);
    network.putEvent(event);
    assertFalse(localQueue.isEmpty());
    assertFalse(peers.wasCalled());
  }

  @Test
  void remoteDestinationForwardsToPeer() {
    Event<Field> event = new Event<>(0, 1, 1, 1, 2, 1);
    event.setData(new Field());
    network.putEvent(event);
    assertTrue(localQueue.isEmpty());
    assertTrue(peers.wasCalled());
  }

  private static final class RecordingPeerRegistry extends WorkstationPeerRegistry {
    private final AtomicBoolean called = new AtomicBoolean();

    @Override
    public void forwardEvent(String targetWorkstationId, long jobId, Event<Field> event) {
      called.set(true);
    }

    boolean wasCalled() {
      return called.get();
    }
  }
}
