package rs.ac.bg.etf.kdp.simulation.components;

import java.util.LinkedList;
import java.util.List;

import rs.ac.bg.etf.sleep.simulation.*;

/**
 * Aggregator component that merges partial {@link Field} results from workers.
 *
 * <p>The Collector (typically component id 2) accumulates worker outputs until
 * {@link #cnt} equals {@link #m} (total body count), then merges all bodies and
 * sends the combined state back to the {@link Bag} (component id 1).
 */
public class Collector extends G {
	/** Running count of bodies received in the current aggregation round. */
	int cnt;

	/** Partial field results collected in the current round. */
	List<Field> filds;

	/** Whether this is the first message in a new aggregation round. */
	boolean start;

	/** Number of workers (configuration field). */
	int n;

	/** Total number of bodies expected per round. */
	int m;

	/** Creates a collector with default state and an empty partial-result list. */
	public Collector() {
		id = 0;
		name = "";
		lTime = 0;
		cnt = 0;
		filds = new LinkedList<Field>();
		start = true;
		n = 0;
		m = 0;
	}

	/**
	 * Accumulates partial {@link Field} results from workers and merges when complete.
	 *
	 * <p>When {@link #cnt} reaches {@link #m}, all bodies are merged into one {@link Field}
	 * and sent to the Bag on component id 1, port 0.
	 *
	 * @param msg incoming partial result from a worker
	 * @return list containing the merged result event to the Bag, or empty while accumulating
	 */
	@Override
	public List<Event<Field>> execute(Event<Field> msg) {
		List<Event<Field>> result = new LinkedList<Event<Field>>();
		if (msg.getSrcID() != id) {
			if (start) {
				lTime = msg.getlTime();
				start = false;
			}
			Field field = msg.getData();
			if (field != null) {
				cnt = cnt + field.indexes.size();
				filds.add(field);
				if (cnt == m) {
					Field resultField = new Field();
					for (Field f : filds) {
						for (Body b : f.coordinates) {
							resultField.addBody(b);
							resultField.addIndex(b.id);
						}
					}
					resultField.interval = field.interval;
					resultField.iteration = field.iteration + 1;
					resultField.time = lTime + field.interval;
					Event<Field> resultMsg = new Event<Field>();
					resultMsg.setData(resultField);
					resultMsg.setId(msg.getId() + 1);
					resultMsg.setSrcID(id);
					resultMsg.setSrcPort(1);
					resultMsg.setDstID(1);
					resultMsg.setDstPort(0);
					resultMsg.setlTime(lTime);
					resultMsg.setlTimeCreated(lTime);
					result.add(resultMsg);
					start = true;
					cnt = 0;
					filds = new LinkedList<Field>();
				}
			}
		}
		if (result.size() == 0) {
			// result.add(createForItself());
		}
		return result;
	}

	/**
	 * Serializes collector configuration state.
	 *
	 * @return string array: id, class name, name, id, n, m
	 */
	@Override
	public String[] getState() {
		String[] result = new String[6];
		result[0] = "" + id;
		result[1] = this.getClass().getName();
		result[2] = name;
		result[3] = "" + id;
		result[4] = "" + n;
		result[5] = "" + m;
		return result;
	}

	/**
	 * Restores collector configuration from a serialized string array.
	 *
	 * @param args serialized state from {@link #getState()}
	 */
	@Override
	public void setState(String[] args) {
		name = args[2];
		id = Integer.parseInt(args[3]);
		n = Integer.parseInt(args[4]);
		m = Integer.parseInt(args[5]);
	}

	/**
	 * Rolls logical time back to the given value.
	 *
	 * @param time logical time to restore
	 */
	@Override
	public void restart(long time) {
		lTime = time;
	}

}
