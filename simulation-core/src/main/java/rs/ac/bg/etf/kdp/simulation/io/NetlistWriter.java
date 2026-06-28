package rs.ac.bg.etf.kdp.simulation.io;

import java.io.FileWriter;
import java.io.PrintWriter;

import rs.ac.bg.etf.sleep.simulation.Netlist;
import rs.ac.bg.etf.sleep.simulation.SimComponent;

/**
 * Writes the final state of a simulated {@link Netlist} to a text file.
 *
 * <p>Each component is written on one line; state values from
 * {@link SimComponent#getState()} are joined by spaces.
 */
public final class NetlistWriter {

    private NetlistWriter() {
    }

    /**
     * Persists component states from the netlist to the given output file.
     *
     * @param netlist netlist whose component states should be written
     * @param outputPath destination file path
     * @throws Exception if the output file cannot be created or written
     */
    public static void write(Netlist<?> netlist, String outputPath) throws Exception {
        PrintWriter out = new PrintWriter(new FileWriter(outputPath));
        for (SimComponent<?> component : netlist.getComponents().values()) {
            String[] state = component.getState();
            StringBuilder line = new StringBuilder();
            for (String value : state) {
                if (line.length() > 0) {
                    line.append(' ');
                }
                line.append(value);
            }
            out.println(line);
        }
        out.close();
    }
}
