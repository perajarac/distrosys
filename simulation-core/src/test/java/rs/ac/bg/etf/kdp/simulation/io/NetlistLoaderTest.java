package rs.ac.bg.etf.kdp.simulation.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import rs.ac.bg.etf.sleep.simulation.Netlist;

/**
 * Unit tests for {@link NetlistLoader}.
 */
class NetlistLoaderTest {

    @Test
    void loadMiniDatasetCreatesFourComponents() throws Exception {
        Path miniDir = resourceDirectory("mini");
        Netlist<Object> netlist = NetlistLoader.load(
                miniDir.resolve("komponente.txt").toString(),
                miniDir.resolve("veze.txt").toString());

        assertEquals(4, netlist.getComponents().size());
        assertNotNull(netlist.getComponent(1));
        assertNotNull(netlist.getComponent(2));
        assertNotNull(netlist.getComponent(3));
        assertNotNull(netlist.getComponent(4));
        assertEquals(9, netlist.getConnections().size());
    }

    private static Path resourceDirectory(String name) throws Exception {
        URL url = NetlistLoaderTest.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + name);
        }
        return Path.of(url.toURI());
    }
}
