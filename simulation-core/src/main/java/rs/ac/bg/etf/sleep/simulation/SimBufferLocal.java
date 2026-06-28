package rs.ac.bg.etf.sleep.simulation;

import java.util.*;

/**
 * Local {@link SimBuffer} backed by a {@link PriorityQueue} ordered by {@link Event#compareTo(Event)}.
 *
 * @param <T> payload type of stored events
 */
public class SimBufferLocal<T> implements SimBuffer<T> {
	PriorityQueue<Event<T>> queue;
	Netlist<T> simulation;

	/** Creates an empty local event buffer. */
	public SimBufferLocal() {
		queue = new PriorityQueue<Event<T>>();
	}

	/**
	 * Removes all events with logical time strictly less than the given time.
	 *
	 * <p>Iterates the queue and removes matching events; behavior during concurrent
	 * modification of the underlying queue is undefined.
	 *
	 * @param time cutoff logical time
	 */
	public void cancel(long time) {
		for (Event<T> msg : queue) {
			if (msg.lTime < time) {// ??
				queue.remove(msg);
			}
		}
	}

	/**
	 * Associates this buffer with a netlist (optional back-reference).
	 *
	 * @param simulation netlist to link
	 */
	public void setSimulation(Netlist<T> simulation) {
		this.simulation = simulation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putEvent(Event<T> event) {
		queue.add(event);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putEvents(List<Event<T>> events) {
		for (Event<T> event : events) {
			putEvent(event);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Event<T> getEvent() {
		return queue.remove();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Event<T>> getEvents() {
		List<Event<T>> list = new LinkedList<Event<T>>();
		list.add(getEvent());
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Currently returns {@code 0} unconditionally; minimum-rank tracking is not implemented.
	 *
	 * @return {@code 0}
	 */
	@Override
	public long getMinrank() {
		return 0;
	}

}
