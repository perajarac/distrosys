package rs.ac.bg.etf.job;

import rs.ac.bg.etf.kdp.simulation.io.NetlistLoader;
import rs.ac.bg.etf.sleep.simulation.Netlist;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Validates submitted job input files by loading them as a simulation netlist.
 */
public class JobValidator {

    /**
     * Validates that component and connection files exist and describe a usable netlist.
     *
     * @param componentsPath path to the components file
     * @param connectionsPath path to the connections file
     * @return validation outcome with a message on failure
     */
    public ValidationResult validate(String componentsPath, String connectionsPath) {
        if (componentsPath == null || componentsPath.isBlank()) {
            return ValidationResult.failure("components path is required");
        }
        if (connectionsPath == null || connectionsPath.isBlank()) {
            return ValidationResult.failure("connections path is required");
        }
        Path components = Path.of(componentsPath);
        Path connections = Path.of(connectionsPath);
        if (!Files.isRegularFile(components)) {
            return ValidationResult.failure("components file not found: " + componentsPath);
        }
        if (!Files.isRegularFile(connections)) {
            return ValidationResult.failure("connections file not found: " + connectionsPath);
        }
        try {
            Netlist<Object> netlist = NetlistLoader.load(componentsPath, connectionsPath);
            if (netlist.getComponents().isEmpty()) {
                return ValidationResult.failure("netlist contains no components (check class names and file format)");
            }
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.failure("invalid netlist: " + e.getMessage());
        }
    }
}
