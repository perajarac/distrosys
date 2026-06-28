package rs.ac.bg.etf.kdp.simulation.io;

import rs.ac.bg.etf.sleep.simulation.Netlist;
import rs.ac.bg.etf.sleep.simulation.Simulator;

/**
 * Command-line entry point for running a local simulation end to end.
 *
 * <p>Loads a netlist, runs the selected simulator until the logical time
 * reaches {@code endTime}, then writes component states via {@link NetlistWriter}.
 *
 * <p>Usage:
 * {@code <komponente.txt> <veze.txt> <output.txt> <simType> <endTime>}
 */
public final class SimulationRunner {

    private SimulationRunner() {
    }

    /**
     * Runs a simulation from command-line arguments.
     *
     * <p>Arguments:
     * <ol>
     *   <li>components file path</li>
     *   <li>connections file path</li>
     *   <li>output file path</li>
     *   <li>simulation type ({@link SimulationType#name()})</li>
     *   <li>end time (exclusive upper bound for {@link Simulator#getlTime()})</li>
     * </ol>
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println(
                    "Usage: <komponente.txt> <veze.txt> <output.txt> <simType> <endTime>");
            System.exit(1);
        }

        try {
            String componentsPath = args[0];
            String connectionsPath = args[1];
            String outputPath = args[2];
            SimulationType simType = SimulationType.fromString(args[3]);
            long endTime = Long.parseLong(args[4]);

            Netlist<Object> netlist = NetlistLoader.load(componentsPath, connectionsPath);
            Simulator<Object> simulator = (Simulator<Object>) SimulatorFactory.create(simType, 1);

            simulator.setNetlist(netlist);
            simulator.init();
            while (simulator.getlTime() < endTime) {
                simulator.execute();
            }
            NetlistWriter.write(netlist, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
