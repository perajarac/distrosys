package rs.ac.bg.etf.sleep.simulation;

import java.util.*;

/**
 * Abstract discrete-event simulation engine.
 *
 * <p>Maintains a {@link Netlist} of {@link SimComponent} instances, an event
 * {@link SimBuffer}, and timing/audit state. Subclasses implement {@link #execute()}
 * to define how events are dequeued and processed.
 *
 * @param <V> payload type of events in this simulation
 */
public abstract class Simulator<V> {
	int id;

	long pTime;
	boolean end;
	long lTime;

	Netlist<V> netlist;
	SimBuffer<V> queue;
	List<Event<V>> pastReceivedEvent;
	List<Event<V>> pastCreatedEvent;
	Event<V> lastEvent;

	long iteration;
	long iterationTime;

	/**
	 * Creates a simulator with a new empty netlist and local event queue.
	 *
	 * @param id simulator identifier
	 */
	public Simulator(int id) {
		pTime = 0;
		end = false;
		queue = new SimBufferLocal<V>();
		lTime = 0;
		pastReceivedEvent = new LinkedList<Event<V>>();
		pastCreatedEvent = new LinkedList<Event<V>>();
		lastEvent = null;
		iteration = 0;
		netlist = new Netlist<V>();
		iterationTime = 0;
	}

	/**
	 * Initializes all netlist components and enqueues their bootstrap events.
	 */
	public void init() {
		Long[] keys = netlist.getComponents().keySet().toArray(
				new Long[netlist.getComponents().size()]);
		for (Long key : keys) {
			SimComponent<V> comp = netlist.getComponents().get(key);
			List<Event<V>> events = comp.init();
			queue.putEvents(events);
			pastCreatedEvent.addAll(0, events);
		}
	}

	/**
	 * Runs the simulation until {@link #end} is set to {@code true}.
	 *
	 * <p>Repeatedly calls {@link #execute()} and records wall-clock duration per iteration
	 * in {@link #iterationTime}.
	 */
	public void simulate() {
		while (!end) {
			long start = System.currentTimeMillis();
			execute();
			long end = System.currentTimeMillis();
			iterationTime = end - start;
		}
	}

	/**
	 * Executes one simulation step if the queue is non-empty.
	 *
	 * @return {@code false} if the queue is empty; {@code true} after processing one event
	 */
	public boolean loop() {
		if (queue.isEmpty()) {
			return false;
		}

		execute();

		pTime += calculateTime();
		return true;
	}

	/**
	 * Processes the next event according to the concrete simulator strategy.
	 */
	public abstract void execute();

	/**
	 * Records a copy of the last received event in the audit history.
	 *
	 * @param lastEvent event that was just processed
	 */
	protected void pastEvents(Event<V> lastEvent) {
		pastReceivedEvent.add(0, lastEvent.copy());
	}

	/**
	 * Dispatches an event to its destination component and enqueues resulting events.
	 *
	 * @param event event to process
	 */
	public void work(Event<V> event) {
		SimComponent<V> comp = netlist.getComponent(event.dstID);
		List<Event<V>> events = comp.execute(event);
		queue.putEvents(netlist.transform(events));
		pastCreatedEvent.addAll(0, events);
		iteration++;
	}

	/**
	 * Returns the wall-clock duration of the last iteration in milliseconds.
	 *
	 * @return last iteration time
	 */
	public long calculateTime() {
		return iterationTime;
	}

	/**
	 * Returns the simulator identifier.
	 *
	 * @return simulator id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the simulator identifier.
	 *
	 * @param id simulator id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns accumulated physical (wall-clock) time.
	 *
	 * @return physical time
	 */
	public long getpTime() {
		return pTime;
	}

	/**
	 * Sets accumulated physical (wall-clock) time.
	 *
	 * @param pTime physical time
	 */
	public void setpTime(long pTime) {
		this.pTime = pTime;
	}

	/**
	 * Returns whether the simulation loop should terminate.
	 *
	 * @return {@code true} if simulation has ended
	 */
	public boolean isEnd() {
		return end;
	}

	/**
	 * Sets whether the simulation loop should terminate.
	 *
	 * @param end {@code true} to stop the simulation
	 */
	public void setEnd(boolean end) {
		this.end = end;
	}

	/**
	 * Returns the current logical simulation time.
	 *
	 * @return logical time
	 */
	public long getlTime() {
		return lTime;
	}

	/**
	 * Sets the current logical simulation time.
	 *
	 * @param lTime logical time
	 */
	public void setlTime(long lTime) {
		this.lTime = lTime;
	}

	/**
	 * Returns the component netlist.
	 *
	 * @return netlist
	 */
	public Netlist<V> getNetlist() {
		return netlist;
	}

	/**
	 * Sets the component netlist.
	 *
	 * @param netlist netlist to use
	 */
	public void setNetlist(Netlist<V> netlist) {
		this.netlist = netlist;
	}

	/**
	 * Returns the event queue.
	 *
	 * @return event buffer
	 */
	public SimBuffer<V> getQueue() {
		return queue;
	}

	/**
	 * Sets the event queue.
	 *
	 * @param queue event buffer to use
	 */
	public void setQueue(SimBuffer<V> queue) {
		this.queue = queue;
	}

	/**
	 * Returns the history of received events (most recent first).
	 *
	 * @return received-event audit list
	 */
	public List<Event<V>> getPastReceivedEvent() {
		return pastReceivedEvent;
	}

	/**
	 * Sets the history of received events.
	 *
	 * @param pastReceivedEvent received-event audit list
	 */
	public void setPastReceivedEvent(List<Event<V>> pastReceivedEvent) {
		this.pastReceivedEvent = pastReceivedEvent;
	}

	/**
	 * Returns the history of created events (most recent first).
	 *
	 * @return created-event audit list
	 */
	public List<Event<V>> getPastCreatedEvent() {
		return pastCreatedEvent;
	}

	/**
	 * Sets the history of created events.
	 *
	 * @param pastCreatedEvent created-event audit list
	 */
	public void setPastCreatedEvent(List<Event<V>> pastCreatedEvent) {
		this.pastCreatedEvent = pastCreatedEvent;
	}

	/**
	 * Returns the most recently processed event.
	 *
	 * @return last event, or {@code null} if none
	 */
	public Event<V> getLastEvent() {
		return lastEvent;
	}

	/**
	 * Sets the most recently processed event.
	 *
	 * @param lastEvent last event
	 */
	public void setLastEvent(Event<V> lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * Returns the number of completed work iterations.
	 *
	 * @return iteration count
	 */
	public long getIteration() {
		return iteration;
	}

	/**
	 * Sets the iteration count.
	 *
	 * @param iteration iteration count
	 */
	public void setIteration(long iteration) {
		this.iteration = iteration;
	}

}
