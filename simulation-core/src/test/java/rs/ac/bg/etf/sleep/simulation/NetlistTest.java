package rs.ac.bg.etf.sleep.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.kdp.simulation.components.Field;

/** Unit tests for {@link Netlist} component registration and event routing. */
class NetlistTest {

  private Netlist<Field> netlist;

  @BeforeEach
  void setUp() {
    netlist = new Netlist<>();
    netlist.addComponent(
        new String[] {
          "1", "rs.ac.bg.etf.kdp.simulation.components.Bag", "Bag", "1", "2", "1", "0", "1",
          "1", "1", "2", "3", "1", "2", "3", "3"
        });
    netlist.addComponent(
        new String[] {
          "2", "rs.ac.bg.etf.kdp.simulation.components.Collector", "Collector", "2", "2", "1"
        });
    netlist.addComponent(
        new String[] {"3", "rs.ac.bg.etf.kdp.simulation.components.Worker", "Worker1", "3"});
    netlist.addComponent(
        new String[] {"4", "rs.ac.bg.etf.kdp.simulation.components.Worker", "Worker1", "4"});

    netlist.addConnection(1, 1, 3, 0);
    netlist.addConnection(1, 2, 4, 0);
    netlist.addConnection(2, 1, 1, 0);
  }

  @Test
  void addComponentRegistersInstances() {
    assertEquals(4, netlist.getComponents().size());
    assertNotNull(netlist.getComponent(1));
    assertNotNull(netlist.getComponent(2));
  }

  @Test
  void transformRoutesBagWorkerPortToCorrectDestination() {
    Event<Field> outgoing = new Event<>(0, 0, 1, 1, 0, 0);
    outgoing.setSrcID(1);
    outgoing.setSrcPort(1);

    List<Event<Field>> routed = netlist.transform(List.of(outgoing));

    assertEquals(1, routed.size());
    assertEquals(3, routed.get(0).getDstID());
    assertEquals(0, routed.get(0).getDstPort());
  }

  @Test
  void transformRoutesCollectorBackToBag() {
    Event<Field> outgoing = new Event<>(0, 0, 2, 1, 0, 0);
    outgoing.setSrcID(2);
    outgoing.setSrcPort(1);

    List<Event<Field>> routed = netlist.transform(List.of(outgoing));

    assertEquals(1, routed.size());
    assertEquals(1, routed.get(0).getDstID());
    assertEquals(0, routed.get(0).getDstPort());
  }
}
