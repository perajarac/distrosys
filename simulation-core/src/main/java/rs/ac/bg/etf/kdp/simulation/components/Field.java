package rs.ac.bg.etf.kdp.simulation.components;

import java.io.*;
import java.util.*;

/**
 * Serializable work unit carrying a subset of bodies for one gravitational simulation step.
 *
 * <p>A {@link Field} holds the full coordinate list plus indexes of bodies assigned to a worker.
 * {@link #calculate()} advances only those bodies using Newtonian gravity.
 */
public class Field implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Gravitational constant used in force calculations (scaled units). */
  private static final double GAMA = 6.674 / 100000000000l;

  /** Simulation iteration counter associated with this field. */
  long iteration;

  /** Current logical time of the field. */
  long time;

  /** Time step (interval) for this computation. */
  long interval;

  /** Full list of body coordinates visible to this computation. */
  List<Body> coordinates;

  /** Indexes into {@link #coordinates} identifying bodies to advance. */
  List<Integer> indexes;

  /** Creates an empty field with zero iteration, time, and interval. */
  public Field() {
    iteration = 0;
    time = 0;
    interval = 0;
    coordinates = new LinkedList<Body>();
    indexes = new LinkedList<Integer>();
  }

  /**
   * Advances all bodies indexed in {@link #indexes} by one time step.
   *
   * @return a new field containing updated body states and incremented time
   */
  public Field calculate() {
    Field result = new Field();
    List<Body> bodies = new LinkedList<Body>();
    for (int i : indexes) {
      bodies.add(move(coordinates, coordinates.get(i)));
    }
    result.indexes = indexes;
    result.time = time + interval;
    result.interval = interval;
    result.coordinates = bodies;
    return result;
  }

  /**
   * Computes the next state of a single body under gravitational forces from all other bodies.
   *
   * @param coordinates full list of bodies used as gravitational sources
   * @param body body to advance
   * @return a new body with updated position, velocity, and acceleration
   */
  public Body move(List<Body> coordinates, Body body) {
    Body result = new Body();
    double ax = 0;
    double ay = 0;
    double az = 0;
    for (Body b : coordinates) {
      if (b != body) {
        double r = distance(body, b);
        r = r * r * r;
        ax += b.m * (b.x - body.x) / r;
        ay += b.m * (b.y - body.y) / r;
        az += b.m * (b.z - body.z) / r;
      }
    }
    result.ax = -ax * GAMA;
    result.ay = -ay * GAMA;
    result.az = -az * GAMA;
    result.m = body.m;
    result.id = body.id;
    result.vx = body.vx + ax * interval;
    result.vy = body.vy + ay * interval;
    result.vz = body.vz + az * interval;
    result.x += body.x + body.vx * interval + ax * interval * interval / 2;
    result.y += body.y + body.vx * interval + ay * interval * interval / 2;
    result.z += body.z + body.vx * interval + az * interval * interval / 2;
    return result;
  }

  /**
   * Returns the Euclidean distance between two bodies.
   *
   * <p>Distances below {@code 1e-10} are clamped to {@code 1e-10} to avoid division by zero.
   *
   * @param a first body
   * @param b second body
   * @return distance between the two bodies
   */
  public double distance(Body a, Body b) {
    double r = 0;
    double a1 = a.x - b.x;
    double a2 = a.y - b.y;
    double a3 = a.z - b.z;
    r = Math.sqrt(a1 * a1 + a2 * a2 + a3 * a3);
    if (r < 10E-10) r = 10E-10;
    return r;
  }

  /**
   * Appends a body to the coordinate list.
   *
   * @param b body to add
   */
  public void addBody(Body b) {
    coordinates.add(b);
  }

  /**
   * Appends an index to the list of bodies to compute.
   *
   * @param index index into {@link #coordinates}
   */
  public void addIndex(int index) {
    indexes.add(index);
  }

  /**
   * @return simulation iteration counter
   */
  public long getIteration() {
    return iteration;
  }

  /**
   * @param iteration simulation iteration counter
   */
  public void setIteration(long iteration) {
    this.iteration = iteration;
  }

  /**
   * @return current logical time
   */
  public long getTime() {
    return time;
  }

  /**
   * @param time current logical time
   */
  public void setTime(long time) {
    this.time = time;
  }

  /**
   * @return time step for this computation
   */
  public long getInterval() {
    return interval;
  }

  /**
   * @param interval time step for this computation
   */
  public void setInterval(long interval) {
    this.interval = interval;
  }

  /**
   * @return body coordinates visible to this computation
   */
  public List<Body> getCoordinates() {
    return coordinates;
  }

  /**
   * @param coordinates body coordinates visible to this computation
   */
  public void setCoordinates(List<Body> coordinates) {
    this.coordinates = coordinates;
  }

  /**
   * @return indexes of bodies to advance
   */
  public List<Integer> getIndexes() {
    return indexes;
  }

  /**
   * @param indexes indexes of bodies to advance
   */
  public void setIndexes(List<Integer> indexes) {
    this.indexes = indexes;
  }
}
