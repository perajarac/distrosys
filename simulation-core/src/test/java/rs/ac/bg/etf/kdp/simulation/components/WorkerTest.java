package rs.ac.bg.etf.kdp.simulation.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rs.ac.bg.etf.sleep.simulation.Event;

/**
 * Unit tests for {@link Worker} field computation and collector routing.
 */
class WorkerTest {

    private Worker worker;

    @BeforeEach
    void setUp() {
        worker = new Worker();
        worker.id = 3;
    }

    @Test
    void executeReturnsEventToCollector() {
        Field field = new Field();
        field.interval = 1;
        field.time = 0;
        Body body = new Body();
        body.id = 0;
        body.m = 1;
        body.x = 2;
        body.y = 3;
        body.z = 1;
        body.vx = 2;
        body.vy = 3;
        body.vz = 3;
        field.coordinates = Arrays.asList(body);
        field.indexes = Arrays.asList(0);

        Event<Field> task = new Event<>(0, 0, 1, 1, 3, 0);
        task.setSrcID(1);
        task.setData(field);

        List<Event<Field>> result = worker.execute(task);

        assertEquals(1, result.size());
        Event<Field> response = result.get(0);
        assertEquals(2, response.getDstID());
        assertEquals(2, response.getDstPort());
        assertEquals(3, response.getSrcID());
        assertEquals(1, response.getlTime());
    }

    @Test
    void executeIgnoresSelfOriginatedMessages() {
        Event<Field> selfEvent = new Event<>(0, 0, 3, 0, 3, 0);
        selfEvent.setSrcID(3);

        List<Event<Field>> result = worker.execute(selfEvent);

        assertTrue(result.isEmpty());
    }
}
