package rs.ac.bg.etf.kdp.simulation.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rs.ac.bg.etf.sleep.simulation.Event;

/**
 * Unit tests for {@link Collector} partial-result aggregation.
 */
class CollectorTest {

    private Collector collector;

    @BeforeEach
    void setUp() {
        collector = new Collector();
        collector.id = 2;
        collector.n = 2;
        collector.m = 2;
    }

    private Event<Field> workerResult(int workerId, int bodyId) {
        Field field = new Field();
        field.interval = 1;
        field.iteration = 0;
        Body body = new Body();
        body.id = bodyId;
        body.m = 1;
        body.x = bodyId;
        body.y = bodyId;
        body.z = bodyId;
        field.coordinates = Arrays.asList(body);
        field.indexes = Arrays.asList(bodyId);

        Event<Field> event = new Event<>(0, 1, workerId, 1, 2, workerId - 1);
        event.setSrcID(workerId);
        event.setData(field);
        return event;
    }

    @Test
    void executeAccumulatesUntilBodyCountReached() {
        List<Event<Field>> first = collector.execute(workerResult(3, 0));
        assertTrue(first.isEmpty());

        List<Event<Field>> second = collector.execute(workerResult(4, 1));

        assertEquals(1, second.size());
        Event<Field> toBag = second.get(0);
        assertEquals(1, toBag.getDstID());
        assertEquals(0, toBag.getDstPort());
        assertEquals(2, toBag.getData().coordinates.size());
        assertEquals(0, collector.cnt);
        assertTrue(collector.start);
    }
}
