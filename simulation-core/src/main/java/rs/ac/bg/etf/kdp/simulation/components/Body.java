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
}
