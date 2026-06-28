package rs.ac.bg.etf.kdp.simulation.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import rs.ac.bg.etf.sleep.simulation.Netlist;
import rs.ac.bg.etf.sleep.simulation.Simulator;

/** End-to-end golden tests comparing simulation output to stored baselines. */
class SimulationGoldenTest {

  @TempDir Path tempDir;

  static Stream<Arguments> goldenCases() {
    return Stream.of(
        Arguments.of("komponente2-5000.txt", "veze2-5000.txt", "komponente2-baseline.txt"),
        Arguments.of("komponente3-5000.txt", "veze3-5000.txt", "komponente3-baseline.txt"));
  }

  static Stream<Arguments> slowGoldenCases() {
    return Stream.of(
        Arguments.of("komponente20-5000.txt", "veze20-5000.txt", "komponente20-baseline.txt"));
  }

  @ParameterizedTest
  @MethodSource("goldenCases")
  void simulationMatchesBaseline(String componentsFile, String connectionsFile, String baselineFile)
      throws Exception {
    runGoldenCase(componentsFile, connectionsFile, baselineFile);
  }

  @Tag("slow")
  @ParameterizedTest
  @MethodSource("slowGoldenCases")
  void slowSimulationMatchesBaseline(
      String componentsFile, String connectionsFile, String baselineFile) throws Exception {
    runGoldenCase(componentsFile, connectionsFile, baselineFile);
  }

  private void runGoldenCase(String componentsFile, String connectionsFile, String baselineFile)
      throws Exception {
    Path testDataDir = projectRoot().resolve("test-data");
    Path componentsPath = testDataDir.resolve(componentsFile);
    Path connectionsPath = testDataDir.resolve(connectionsFile);
    Path baselinePath = resourcePath("baseline").resolve(baselineFile);
    Path outputPath = tempDir.resolve("actual.txt");

    Netlist<Object> netlist =
        NetlistLoader.load(componentsPath.toString(), connectionsPath.toString());
    @SuppressWarnings("unchecked")
    Simulator<Object> simulator =
        (Simulator<Object>) SimulatorFactory.create(SimulationType.OPTIMISTIC, 1);
    simulator.setNetlist(netlist);
    simulator.init();

    long endTime = 100;
    while (simulator.getlTime() < endTime) {
      simulator.execute();
    }

    NetlistWriter.write(netlist, outputPath.toString());

    List<String> expected = Files.readAllLines(baselinePath);
    List<String> actual = Files.readAllLines(outputPath);

    assertEquals(expected.size(), actual.size(), "Line count mismatch for " + componentsFile);
    assertLinesMatch(expected, actual);
  }

  private static Path projectRoot() {
    Path moduleDir = Path.of(System.getProperty("user.dir"));
    if (moduleDir.endsWith("simulation-core")) {
      return moduleDir.getParent();
    }
    return moduleDir;
  }

  private static Path resourcePath(String name) throws Exception {
    URL url = SimulationGoldenTest.class.getClassLoader().getResource(name);
    if (url == null) {
      throw new IllegalStateException("Missing test resource directory: " + name);
    }
    return Path.of(url.toURI());
  }
}
