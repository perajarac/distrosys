package rs.ac.bg.etf.sleep.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SimBufferLocal} priority-queue event handling. */
class SimBufferLocalTest {

  private SimBufferLocal<String> buffer;

  @BeforeEach
  void setUp() {
    buffer = new SimBufferLocal<>();
  }

  @Test
  void putAndGetEventReturnsEarliestLogicalTime() {
    Event<String> later = new Event<>(0, 20, 1, 0, 2, 0);
    Event<String> earlier = new Event<>(0, 10, 1, 0, 2, 0);

    buffer.putEvent(later);
    buffer.putEvent(earlier);

    assertFalse(buffer.isEmpty());
    assertEquals(10, buffer.getEvent().getlTime());
    assertEquals(20, buffer.getEvent().getlTime());
    assertTrue(buffer.isEmpty());
  }

  @Test
  void getEventsReturnsSingletonList() {
    Event<String> event = new Event<>(0, 5, 1, 0, 2, 0);
    buffer.putEvent(event);

    List<Event<String>> events = buffer.getEvents();
    assertEquals(1, events.size());
    assertEquals(5, events.get(0).getlTime());
    assertTrue(buffer.isEmpty());
  }
}
