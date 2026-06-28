package rs.ac.bg.etf.kdp.simulation.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Field} gravitational step calculations. */
class FieldTest {

  private Field field;
  private Body body1;
  private Body body2;

  @BeforeEach
  void setUp() {
    field = new Field();
    field.interval = 1;
    field.time = 0;

    body1 = new Body();
    body1.id = 1;
    body1.m = 1;
    body1.x = 2;
    body1.y = 3;
    body1.z = 1;
    body1.vx = 2;
    body1.vy = 3;
    body1.vz = 3;

    body2 = new Body();
    body2.id = 2;
    body2.m = 2;
    body2.x = 3;
    body2.y = 4;
    body2.z = 2;
    body2.vx = 3;
    body2.vy = 4;
    body2.vz = 4;

    field.coordinates = Arrays.asList(body1, body2);
    field.indexes = Arrays.asList(0);
  }

  @Test
  void distanceBetweenTwoBodies() {
    assertEquals(Math.sqrt(3), field.distance(body1, body2), 1e-10);
  }

  @Test
  void calculateAdvancesFirstBodyByOneStep() {
    Field result = field.calculate();

    assertEquals(1, result.time);
    assertEquals(1, result.interval);
    assertEquals(1, result.coordinates.size());

    Body moved = result.coordinates.get(0);
    assertEquals(1, moved.m);
    assertEquals(1, moved.id);
    assertEquals(4.192450089729875, moved.x, 1e-10);
    assertEquals(5.192450089729875, moved.y, 1e-10);
    assertEquals(3.192450089729875, moved.z, 1e-10);
    assertEquals(2.38490017945975, moved.vx, 1e-10);
    assertEquals(3.38490017945975, moved.vy, 1e-10);
    assertEquals(3.38490017945975, moved.vz, 1e-10);
  }

  @Test
  void moveUpdatesPositionAndVelocity() {
    Body moved = field.move(field.coordinates, body1);

    assertEquals(4.192450089729875, moved.x, 1e-10);
    assertEquals(5.192450089729875, moved.y, 1e-10);
    assertEquals(3.192450089729875, moved.z, 1e-10);
    assertEquals(2.38490017945975, moved.vx, 1e-10);
    assertEquals(3.38490017945975, moved.vy, 1e-10);
    assertEquals(3.38490017945975, moved.vz, 1e-10);
  }
}
