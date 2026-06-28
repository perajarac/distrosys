package rs.ac.bg.etf.sleep.simulation;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of simulation components and port-to-port connections.
 *
 * <p>The netlist maps component IDs to {@link SimComponent} instances and wires
 * source endpoints to destination endpoints. Outgoing events are {@link #transform(List)}
 * into one event per connected destination.
 *
 * @param <V> payload type of events routed through this netlist
 */
public class Netlist<V> {
	private static final Logger log = LoggerFactory.getLogger(Netlist.class);

	HashMap<Long, SimComponent<V>> components;
	HashMap<SimEndpoint, List<SimEndpoint>> connections;

	/** Creates an empty netlist with no components or connections. */
	public Netlist() {
		components = new HashMap<Long, SimComponent<V>>();
		connections = new HashMap<SimEndpoint, List<SimEndpoint>>();
	}

	/**
	 * Rewrites outgoing events by copying each event to every destination connected
	 * to its source endpoint.
	 *
	 * @param events events emitted by a component (source routing set)
	 * @return list of events with destination fields resolved from connections
	 */
	public List<Event<V>> transform(List<Event<V>> events) {
		List<Event<V>> result = new LinkedList<Event<V>>();
		for (Event<V> event : events) {
			List<SimEndpoint> endPoints = getEndpoins(event.srcID,
					event.srcPort);
			for (SimEndpoint endPoint : endPoints) {
				Event<V> e = event.copy();
				e.setDstID(endPoint.getComponentID());
				e.setDstPort(endPoint.getComponentPort());
				result.add(e);
			}
		}
		return result;
	}

	/**
	 * Returns destination endpoints connected to the given source endpoint.
	 *
	 * @param srcID   source component identifier
	 * @param srcPort source port number
	 * @return list of connected destinations, or {@code null} if none
	 */
	public List<SimEndpoint> getEndpoins(long srcID, int srcPort) {
		SimEndpoint endPoint = new SimEndpoint(srcID, srcPort);
		return connections.get(endPoint);
	}

	/**
	 * Returns the map of all registered components keyed by identifier.
	 *
	 * @return component registry
	 */
	public HashMap<Long, SimComponent<V>> getComponents() {
		return components;
	}

	/**
	 * Replaces the component registry.
	 *
	 * @param components new component map
	 */
	public void setComponents(HashMap<Long, SimComponent<V>> components) {
		this.components = components;
	}

	/**
	 * Returns the component with the given identifier.
	 *
	 * @param id component identifier
	 * @return the component, or {@code null} if not registered
	 */
	public SimComponent<V> getComponent(long id) {
		return components.get(id);
	}

	/**
	 * Registers or replaces a component under the given identifier.
	 *
	 * @param id        component identifier
	 * @param component component instance
	 */
	public void setComponent(long id, SimComponent<V> component) {
		addComponent(id, component);
	}

	/**
	 * Returns the connection map from source endpoints to destination lists.
	 *
	 * @return connection registry
	 */
	public HashMap<SimEndpoint, List<SimEndpoint>> getConnections() {
		return connections;
	}

	/**
	 * Replaces the connection registry.
	 *
	 * @param connections new connection map
	 */
	public void setConnections(
			HashMap<SimEndpoint, List<SimEndpoint>> connections) {
		this.connections = connections;
	}

	/**
	 * Registers a component under the given identifier.
	 *
	 * @param id        component identifier
	 * @param component component instance
	 */
	public void addComponent(long id, SimComponent<V> component) {
		components.put(id, component);
	}

	/**
	 * Registers multiple components from string-array descriptors.
	 *
	 * @param data array of component descriptor rows
	 */
	public void addComponent(String[][] data) {
		for (String[] x : data) {
			addComponent(x);
		}
	}

	/**
	 * Instantiates and registers a component from a string-array descriptor.
	 *
	 * <p>Expected format: {@code [id, fullyQualifiedClassName, ...stateFields]}.
	 * The class must have a no-arg constructor and implement {@link SimComponent}.
	 *
	 * @param data single component descriptor row
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addComponent(String[] data) {
		try {
			long id = Long.parseLong(data[0]);
			Class c = Class.forName(data[1]);
			SimComponent<V> component = (SimComponent<V>) c.getDeclaredConstructor().newInstance(); // Java 9+
			// SimComponent<V> component = (SimComponent<V>) c.newInstance(); // do Java 9
			component.setState(data);
			components.put(id, component);
		} catch (Exception e) {
			log.error("Failed to add component from data {}", Arrays.toString(data), e);
		}

	}

	/**
	 * Adds multiple port connections from string-array descriptors.
	 *
	 * @param data array of rows {@code [srcID, srcPort, dstID, dstPort]}
	 */
	public void addConnection(String[][] data) {
		for (String[] x : data) {
			addConnection(Long.parseLong(x[0]), Integer.parseInt(x[1]), Long
					.parseLong(x[2]), Integer.parseInt(x[3]));
		}
	}

	/**
	 * Connects a source endpoint to a destination endpoint.
	 *
	 * @param srcID   source component identifier
	 * @param srcPort source port number
	 * @param dstID   destination component identifier
	 * @param dstPort destination port number
	 */
	public void addConnection(long srcID, int srcPort, long dstID, int dstPort) {
		SimEndpoint srcEndPoint = new SimEndpoint(srcID, srcPort);
		SimEndpoint dstEndPoint = new SimEndpoint(dstID, dstPort);
		List<SimEndpoint> endPoints = connections.get(srcEndPoint);
		if (endPoints == null) {
			endPoints = new LinkedList<SimEndpoint>();
			connections.put(srcEndPoint, endPoints);
		}
		endPoints.add(dstEndPoint);
	}

	/**
	 * Serializes the state of all registered components.
	 *
	 * @return array of per-component state string arrays
	 */
	public String[][] getState() {
		String[][] state = new String[components.keySet().size()][];
		int i = 0;
		for (Long l : components.keySet()) {
			state[i++] = components.get(l).getState();
		}
		return state;
	}

}
