package rs.ac.bg.etf.kdp.simulation.components;

import java.util.*;

import rs.ac.bg.etf.sleep.simulation.*;


/**
 * Coordinator component that shards n-body work across workers and advances simulation time.
 * <p>The Bag (typically component id 1) holds the full body list, creates {@link Field}
 * tasks for {@link #n} workers via {@link #createTasksForWorker()}, and accepts merged
 * results from the {@link Collector}. When all tasks for a step are dispatched, it
 * schedules a self-event to continue the loop.
 */
public class Bag extends G {

	/** Maximum number of bodies per worker task. */
	public static int NUM = 1000;

	/** Number of worker nodes. */
	int n;

	/** Total number of bodies in the simulation. */
	int m;

	/** Current list of body states. */
	List<Body> bodies;

	/** History of body snapshots from completed iterations. */
	List<List<Body>> results;

	/** Index of the next body to assign in the current sharding round. */
	int cnt;

	/** Next worker port (1..{@link #n}) to assign a task to. */
	int next;

	/** Wall-clock timestamp used for adaptive {@link #waitPeriod} calculation. */
	long start;

	/** Creates a Bag with default configuration (one worker, one body). */
	public Bag() {
		name = "";
		id = 0;
		n = 1;
		m = 1;
		lTime = 0;
		dt = 0;
		iteration = 0;
		bodies = new LinkedList<Body>();
		results = new LinkedList<List<Body>>();
		cnt = 0;
		next = 1;
		waitPeriod = 10;
		start = System.currentTimeMillis();
	}

	/**
	 * Processes an incoming event: merges collector results or shards work to workers.
	 *
	 * <p>External messages update {@link #bodies} and advance {@link #lTime}. The method
	 * then emits worker tasks and, when {@link #n} tasks are ready, a self-reschedule event.
	 *
	 * @param msg incoming event (collector result or self-trigger)
	 * @return worker task events and optionally a self-event
	 */
	@Override
	public List<Event<Field>> execute(Event<Field> msg) {
		List<Event<Field>> result = new LinkedList<Event<Field>>();
		if (msg.getSrcID() != id) {
			bodies = msg.getData().coordinates;
			lTime += dt;
			iteration++;
			results.add(bodies);
			waitPeriod = (waitPeriod + (System.currentTimeMillis() - start) / (n + 1)) / 2;
			start = System.currentTimeMillis();
			cnt = 0;
			next = 1;
		}
		result.addAll(createTasksForWorker());
		if (result.size() == n) {
			result.add(createForItself());
		}
		return result;
	}

	/**
	 * Creates {@link Field} task events for workers from unassigned bodies in {@link #bodies}.
	 * <p>Each task covers up to {@link #NUM} bodies and is routed to worker {@code next + 2}
	 * on port 0. Worker selection rotates through ports 1..{@link #n}.
	 * @return list of task events (may be empty if all bodies are assigned)
	 */
	public List<Event<Field>> createTasksForWorker() {
		List<Event<Field>> result = new LinkedList<Event<Field>>();
		int i = 0;
		while (cnt < bodies.size() && i < NUM * n) {
			List<Integer> indexes = new LinkedList<Integer>();
			long num = Math.min(NUM, bodies.size() - cnt);
			i += num;
			if (num != 0) {
				Field resultField = new Field();
				for (int j = 0; j < num; j++) {
					indexes.add(cnt++);
				}
				resultField.coordinates = bodies;
				resultField.indexes = indexes;
				resultField.interval = dt;
				resultField.iteration = iteration;
				resultField.time = lTime;

				Event<Field> resultMsg = new Event<Field>();
				resultMsg.setData(resultField);
				resultMsg.setId(id);
				resultMsg.setSrcID(id);
				resultMsg.setSrcPort(next);
				resultMsg.setDstID(next + 2);
				resultMsg.setDstPort(0);
				resultMsg.setlTime(lTime);
				resultMsg.setlTimeCreated(lTime);
				result.add(resultMsg);
				next = (next % n) + 1;
			} else {
				break;
			}
		}
		return result;
	}
	
	/**
	 * Serializes Bag state including all body coordinates.
	 * @return string array: id, class name, name, id, n, m, lTime, dt, then per-body fields
	 */
	@Override
	public String[] getState() {
		String[] result = new String[8 + bodies.size() * 7];
		int i = 0;
		result[i++] = "" + id;
		result[i++] = this.getClass().getName();
		result[i++] = name;
		result[i++] = "" + id;
		result[i++] = "" + n;
		result[i++] = "" + m;
		result[i++] = "" + lTime;
		result[i++] = "" + dt;
		for (Body b : bodies) {
			result[i++] = "" + b.m;
			result[i++] = "" + b.x;
			result[i++] = "" + b.y;
			result[i++] = "" + b.z;
			result[i++] = "" + b.vx;
			result[i++] = "" + b.vy;
			result[i++] = "" + b.vz;
		}
		return result;
	}
	/**
	 * Restores Bag state and body list from a serialized string array.
	 * @param args serialized state from {@link #getState()}
	 */
	@Override
	public void setState(String[] args) {
		name = args[2];
		id = Integer.parseInt(args[3]);
		n = Integer.parseInt(args[4]);
		m = Integer.parseInt(args[5]);
		lTime = Long.parseLong(args[6]);
		dt = Long.parseLong(args[7]);
		for (int j = 0, i = 8; j < m; j++) {
			Body b = new Body();
			b.id = Integer.parseInt(args[i++]);
			b.m = Double.parseDouble(args[i++]);
			b.x = Double.parseDouble(args[i++]);
			b.y = Double.parseDouble(args[i++]);
			b.z = Double.parseDouble(args[i++]);
			b.vx = Double.parseDouble(args[i++]);
			b.vy = Double.parseDouble(args[i++]);
			b.vz = Double.parseDouble(args[i++]);
			bodies.add(b);
		}
	}

	/**
	 * Rolls logical time back to the given value.
	 * @param time logical time to restore
	 */
	@Override
	public void restart(long time) {
		lTime = time;
	}
}
