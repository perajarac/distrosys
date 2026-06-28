package rs.ac.bg.etf.kdp.simulation.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import rs.ac.bg.etf.sleep.simulation.Netlist;

/**
 * Loads a {@link Netlist} from component and connection description files.
 *
 * <p>Component lines are passed to {@link Netlist#addComponent(String[])}.
 * The connections file must contain a header row followed by connection rows,
 * which are forwarded to {@link Netlist#addConnection(String[][])}.
 */
public final class NetlistLoader {

    private NetlistLoader() {
    }

    /**
     * Reads component and connection files and builds a populated netlist.
     *
     * @param componentsPath path to the components file (one component per line)
     * @param connectionsPath path to the connections file (header row plus connections)
     * @return netlist ready for simulation
     * @throws Exception if a file cannot be read or parsed
     */
    public static Netlist<Object> load(String componentsPath, String connectionsPath) throws Exception {
        Netlist<Object> netlist = new Netlist<>();
        BufferedReader in = new BufferedReader(new FileReader(componentsPath));
        String line;
        while ((line = in.readLine()) != null) {
            String[] names = line.split(" ");
            netlist.addComponent(names);
        }
        in.close();

        in = new BufferedReader(new FileReader(connectionsPath));
        List<String[]> connections = new LinkedList<>();
        while ((line = in.readLine()) != null) {
            String[] names = line.split(" ");
            connections.add(names);
        }
        in.close();

        String[][] con = new String[connections.size() - 1][];
        for (int i = 0; i < connections.size() - 1; i++) {
            con[i] = connections.get(i + 1);
        }
        netlist.addConnection(con);
        return netlist;
    }
}
