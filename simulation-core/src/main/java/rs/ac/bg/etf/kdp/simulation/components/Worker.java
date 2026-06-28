package rs.ac.bg.etf.kdp.simulation.components;

import java.util.*;

import rs.ac.bg.etf.sleep.simulation.*;

/**
 * Worker component that executes {@link Field} tasks and returns partial results to the collector.
 *
 * <p>Workers receive sharded {@link Field} work from the {@link Bag}, run
 * {@link Field#calculate()}, and send results to the {@link Collector} (component id 2).
 */
public class Worker extends G {

	/** Creates a worker with default identity and zero logical time. */
	public Worker() {
		id = 0;
		name = "";
		lTime = 0;
	}

	/**
	 * Processes a {@link Field} task from the Bag and returns the computed result to the Collector.
	 *
	 * <p>Ignores self-originated messages. Result is sent to component 2 on port {@code id - 1}
	 * at logical time {@code lTime + field.interval}.
	 *
	 * @param msg incoming task event
	 * @return list containing the result event, or empty if the message was self-originated
	 */
	@Override
	public List<Event<Field>> execute(Event<Field> msg) {
		List<Event<Field>> result = new LinkedList<Event<Field>>();
		if (msg.getSrcID() != id) {
			lTime = msg.getlTime();
			Field field = msg.getData();
			Field resultField = field.calculate();
			Event<Field> resultMsg = new Event<Field>();
			resultMsg.setData(resultField);
			resultMsg.setId(msg.getId() + 1);
			resultMsg.setSrcID(id);
			resultMsg.setSrcPort(1);
			resultMsg.setDstID(2);
			resultMsg.setDstPort(id - 1);
			resultMsg.setlTime(lTime + field.interval);
			resultMsg.setlTimeCreated(lTime);
			result.add(resultMsg);
		}
		if (result.size() == 0) {
			// result.add(createForItself());
		}
		return result;
	}

	/**
	 * Serializes worker identity state.
	 *
	 * @return string array: id, class name, name, id
	 */
	@Override
	public String[] getState() {
		String[] result = new String[4];
		result[0] = "" + id;
		result[1] = this.getClass().getName();
		result[2] = name;
		result[3] = "" + id;
		return result;
	}

	/**
	 * Restores worker identity from a serialized string array.
	 *
	 * @param args serialized state from {@link #getState()}
	 */
	@Override
	public void setState(String[] args) {
		name = args[2];
		id = Integer.parseInt(args[3]);

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
