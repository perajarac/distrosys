package rs.ac.bg.etf.kdp.simulation.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Body} field storage and serialization. */
class BodyTest {

  @Test
  void initializesAndStoresFields() {
    Body body = new Body();
    body.id = 7;
    body.m = 1.5;
    body.x = 2.0;
    body.y = 3.0;
    body.z = 4.0;
    body.vx = 0.1;
    body.vy = 0.2;
    body.vz = 0.3;
    body.ax = 0.01;
    body.ay = 0.02;
    body.az = 0.03;

    assertEquals(7, body.id);
    assertEquals(1.5, body.m);
    assertEquals(2.0, body.x);
    assertEquals(3.0, body.y);
    assertEquals(4.0, body.z);
    assertEquals(0.1, body.vx);
    assertEquals(0.2, body.vy);
    assertEquals(0.3, body.vz);
    assertEquals(0.01, body.ax);
    assertEquals(0.02, body.ay);
    assertEquals(0.03, body.az);
  }

  @Test
  void serializesAndDeserializesCopy() throws Exception {
    Body original = new Body();
    original.id = 1;
    original.m = 2.0;
    original.x = 3.0;
    original.y = 4.0;
    original.z = 5.0;
    original.vx = 6.0;
    original.vy = 7.0;
    original.vz = 8.0;

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
      out.writeObject(original);
    }

    Body copy;
    try (ObjectInputStream in =
        new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
      copy = (Body) in.readObject();
    }

    assertNotSame(original, copy);
    assertEquals(original.id, copy.id);
    assertEquals(original.m, copy.m);
    assertEquals(original.x, copy.x);
    assertEquals(original.y, copy.y);
    assertEquals(original.z, copy.z);
    assertEquals(original.vx, copy.vx);
    assertEquals(original.vy, copy.vy);
    assertEquals(original.vz, copy.vz);
  }
}
