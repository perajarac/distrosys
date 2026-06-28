package rs.ac.bg.etf.kdp.simulation.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.bg.etf.sleep.simulation.Netlist;

/** Unit tests for {@link NetlistWriter} round-trip persistence. */
class NetlistWriterTest {

  @TempDir Path tempDir;

  @Test
  void writePersistsCurrentComponentStates() throws Exception {
    Path miniDir = resourceDirectory("mini");
    Netlist<Object> netlist =
        NetlistLoader.load(
            miniDir.resolve("komponente.txt").toString(), miniDir.resolve("veze.txt").toString());

    Path output = tempDir.resolve("roundtrip.txt");
    NetlistWriter.write(netlist, output.toString());

    String[][] expected = netlist.getState();
    List<String> lines = Files.readAllLines(output);
    assertEquals(expected.length, lines.size());

    for (int i = 0; i < expected.length; i++) {
      assertArrayEquals(expected[i], lines.get(i).split(" "));
    }
  }

  private static Path resourceDirectory(String name) throws Exception {
    URL url = NetlistWriterTest.class.getClassLoader().getResource(name);
    if (url == null) {
      throw new IllegalStateException("Missing test resource directory: " + name);
    }
    return Path.of(url.toURI());
  }
}
