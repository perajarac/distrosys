package rs.ac.bg.etf.kdp.simulation.components;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.bg.etf.sleep.simulation.*;

/**
 * Abstract base class for KDP simulation components operating on {@link Field} payloads.
 *
 * <p>Provides shared timing and identity state plus a default {@link #init()} that schedules a
 * self-directed bootstrap event via {@link #createForItself()}.
 */
public abstract class G implements rs.ac.bg.etf.sleep.simulation.SimComponent<Field> {
  private static final Logger log = LoggerFactory.getLogger(G.class);

  String name;
  int id;
  long lTime;
  long dt;
  long iteration;
  long waitPeriod;

  /** Creates a component with default identity and timing values. */
  public G() {
    name = "";
    id = 0;
    lTime = 0;
    dt = 0;
    iteration = 0;
    waitPeriod = 1000;
  }

  /**
   * Creates a self-directed event after sleeping for {@link #waitPeriod} milliseconds.
   *
   * <p>The returned event routes from this component back to itself with {@code null} payload.
   *
   * @return bootstrap or reschedule event for this component
   */
  public Event<Field> createForItself() {
    try {
      Thread.sleep(waitPeriod);
    } catch (InterruptedException e) {
      log.error("Component {} interrupted while waiting", id, e);
      Thread.currentThread().interrupt();
    }
    Event<Field> resultMsg = new Event<Field>();
    resultMsg.setData(null);
    resultMsg.setId(id);
    resultMsg.setSrcID(id);
    resultMsg.setSrcPort(0);
    resultMsg.setDstID(id);
    resultMsg.setDstPort(0);
    resultMsg.setlTime(lTime);
    resultMsg.setlTimeCreated(lTime);
    return resultMsg;
  }

  /**
   * Returns a single self-directed event to seed the simulation queue.
   *
   * @return list containing one event from {@link #createForItself()}
   */
  public List<Event<Field>> init() {
    List<Event<Field>> result = new LinkedList<Event<Field>>();
    result.add(createForItself());
    return result;
  }
}
