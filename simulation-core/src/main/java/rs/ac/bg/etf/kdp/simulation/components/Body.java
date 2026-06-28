package rs.ac.bg.etf.kdp.simulation.components;

import java.io.*;

/**
 * Serializable data class representing a single body in an n-body gravitational simulation.
 *
 * <p>Stores mass, position, velocity, and acceleration vectors used by {@link Field}
 * when computing gravitational interactions.
 */
public class Body implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Unique identifier of this body within the simulation. */
	int id;

	/** X coordinate of the body's position. */
	double x, y, z;

	/** X, Y, and Z components of the body's velocity. */
	double vx, vy, vz;

	/** X, Y, and Z components of the body's acceleration. */
	double ax, ay, az;

	/** Mass of the body. */
	double m;

	/**
	 * @return unique body identifier
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id unique body identifier
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return body mass
	 */
	public double getM() {
		return m;
	}

	/**
	 * @param m body mass
	 */
	public void setM(double m) {
		this.m = m;
	}

	/**
	 * @return x coordinate
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x x coordinate
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return y coordinate
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y y coordinate
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return z coordinate
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @param z z coordinate
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * @return x velocity component
	 */
	public double getVx() {
		return vx;
	}

	/**
	 * @param vx x velocity component
	 */
	public void setVx(double vx) {
		this.vx = vx;
	}

	/**
	 * @return y velocity component
	 */
	public double getVy() {
		return vy;
	}

	/**
	 * @param vy y velocity component
	 */
	public void setVy(double vy) {
		this.vy = vy;
	}

	/**
	 * @return z velocity component
	 */
	public double getVz() {
		return vz;
	}

	/**
	 * @param vz z velocity component
	 */
	public void setVz(double vz) {
		this.vz = vz;
	}
}
