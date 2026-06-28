package rs.ac.bg.etf.sleep.simulation;

/**
 * Single-threaded {@link Simulator} that processes one event per {@link #execute()} call.
 *
 * @param <V> payload type of events in this simulation
 */
public class SimulatorSinglethread<V> extends Simulator<V> {

	/**
	 * Creates a single-threaded simulator.
	 *
	 * @param id simulator identifier
	 */
	public SimulatorSinglethread(int id) {
		super(id);
	}

	/**
	 * Dequeues the next event, advances logical time, and dispatches it if valid.
	 *
	 * <p>Valid events ({@link Event#ok()}) are passed to {@link #work(Event)} and
	 * recorded in the received-event history.
	 */
	public void execute() {
		lastEvent = queue.getEvent();
		lTime = lastEvent.lTime;
		if (lastEvent.ok()) {
			work(lastEvent);
			pastEvents(lastEvent);
		}
	}

}
