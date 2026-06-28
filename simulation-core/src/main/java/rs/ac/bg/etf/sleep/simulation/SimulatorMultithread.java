package rs.ac.bg.etf.sleep.simulation;

/**
 * Multithreaded {@link Simulator} variant with time-window synchronization (partial implementation).
 *
 * <p>Events outside the current time window are re-queued and {@link #synchronize()} is
 * invoked before retrying. Synchronization and time-range checks are not yet implemented.
 *
 * @param <T> payload type of events in this simulation
 */
public class SimulatorMultithread<T> extends Simulator<T> {

	/**
	 * Creates a multithreaded simulator.
	 *
	 * @param id simulator identifier
	 */
	public SimulatorMultithread(int id) {
		super(id);
	}

	/**
	 * Synchronizes worker threads at a time boundary.
	 *
	 * <p>Not yet implemented.
	 */
	private void synchronize() {
		// TODO Auto-generated method stub
	}

	/**
	 * Returns whether the given event falls within the current time window.
	 *
	 * <p>Not yet implemented; currently always returns {@code false}.
	 *
	 * @param m event to check
	 * @return {@code false} (stub)
	 */
	private boolean isTimeInTheRange(Event<T> m) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Dequeues an event, re-queuing and synchronizing if it is outside the time window.
	 *
	 * <p>Advances logical time and dispatches valid events via {@link #work(Event)}.
	 */
	@Override
	public void execute() {
		Event<T> m = queue.getEvent();
		if (!isTimeInTheRange(m)) {
			queue.putEvent(m);
			synchronize();
			m = queue.getEvent();
		}
		lastEvent = m;
		lTime = lastEvent.lTime;
		if (lastEvent.ok()) {
			work(lastEvent);
		}
	}
}
