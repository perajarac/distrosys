package rs.ac.bg.etf.kdp.simulation.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.sleep.simulation.Netlist;

/** Unit tests for {@link NetlistLoader#loadFromLines}. */
class NetlistLoaderLinesTest {

  @Test
  void loadFromLinesMatchesFileLoadForMiniDataset() throws Exception {
    Path miniDir = resourceDirectory("mini");
    List<String> componentLines = Files.readAllLines(miniDir.resolve("komponente.txt"));
    List<String> connectionLines = Files.readAllLines(miniDir.resolve("veze.txt"));

    Netlist<Object> fromFiles =
        NetlistLoader.load(
            miniDir.resolve("komponente.txt").toString(), miniDir.resolve("veze.txt").toString());
    Netlist<Object> fromLines = NetlistLoader.loadFromLines(componentLines, connectionLines);

    assertEquals(fromFiles.getComponents().size(), fromLines.getComponents().size());
    assertEquals(fromFiles.getConnections().size(), fromLines.getConnections().size());
  }

  private static Path resourceDirectory(String name) throws Exception {
    URL url = NetlistLoaderLinesTest.class.getClassLoader().getResource(name);
    if (url == null) {
      throw new IllegalStateException("Missing test resource directory: " + name);
    }
    return Path.of(url.toURI());
  }
}
