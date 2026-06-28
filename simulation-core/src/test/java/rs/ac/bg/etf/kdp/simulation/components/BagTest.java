package rs.ac.bg.etf.kdp.simulation.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.sleep.simulation.Event;

/** Unit tests for {@link Bag} task sharding and self-reschedule behavior. */
class BagTest {

  private Bag bag;

  @BeforeEach
  void setUp() {
    bag = new Bag();
    bag.id = 1;
    bag.n = 2;
    bag.m = 4;
    bag.dt = 1;
    bag.lTime = 0;
    bag.waitPeriod = 0;

    for (int i = 0; i < 4; i++) {
      Body body = new Body();
      body.id = i;
      body.m = 1;
      body.x = i;
      body.y = i;
      body.z = i;
      bag.bodies.add(body);
    }
    bag.cnt = 0;
    bag.next = 1;
  }

  @Test
  void executeFromSelfCreatesWorkerTaskForAllBodies() {
    Event<Field> selfEvent = new Event<>(0, 0, 1, 0, 1, 0);
    selfEvent.setSrcID(1);

    List<Event<Field>> events = bag.execute(selfEvent);

    assertEquals(1, events.size());
    assertEquals(3, events.get(0).getDstID());
    assertEquals(4, bag.cnt);
    assertEquals(2, bag.next);
  }

  @Test
  void executeFromCollectorResetsShardingAndAdvancesTime() {
    Field merged = new Field();
    merged.coordinates = bag.bodies;
    Event<Field> collectorEvent = new Event<>(0, 1, 2, 1, 1, 0);
    collectorEvent.setSrcID(2);
    collectorEvent.setData(merged);

    List<Event<Field>> events = bag.execute(collectorEvent);

    assertEquals(1, bag.lTime);
    assertEquals(1, bag.iteration);
    assertEquals(4, bag.cnt);
    assertEquals(2, bag.next);
    assertEquals(1, events.size());
    assertTrue(events.get(0).getData().indexes.size() == 4);
  }
}
