package rs.ac.bg.etf.sleep.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Event} ordering, copying, and status. */
class EventTest {

  @BeforeEach
  void resetIdCounter() {
    Event.ID = 0;
  }

  @Test
  void compareToOrdersByLogicalTime() {
    Event<String> earlier = new Event<>(0, 5, 1, 0, 2, 0);
    Event<String> later = new Event<>(0, 10, 1, 0, 2, 0);

    assertTrue(earlier.compareTo(later) < 0);
    assertTrue(later.compareTo(earlier) > 0);
  }

  @Test
  void copyPreservesRoutingAndTiming() {
    Event<String> original = new Event<>(1, 2, 3, 4, 5, 6);
    original.setData("payload");

    Event<String> copy = original.copy();

    assertEquals(original.getlTimeCreated(), copy.getlTimeCreated());
    assertEquals(original.getlTime(), copy.getlTime());
    assertEquals(original.getSrcID(), copy.getSrcID());
    assertEquals(original.getSrcPort(), copy.getSrcPort());
    assertEquals(original.getDstID(), copy.getDstID());
    assertEquals(original.getDstPort(), copy.getDstPort());
    assertEquals(original.getId(), copy.getId());
    assertEquals(original.getData(), copy.getData());
  }

  @Test
  void okReflectsStatus() {
    Event<String> event = new Event<>(0, 0, 0, 0, 0, 0);
    assertTrue(event.ok());

    event.setStatus(Event.NOK);
    assertFalse(event.ok());
  }
}
