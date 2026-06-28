package rs.ac.bg.etf.job;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JobValidatorTest {

  @Test
  void validateAcceptsMiniNetlist() throws Exception {
    Path miniDir = miniDir();
    JobValidator validator = new JobValidator();
    ValidationResult result =
        validator.validate(
            miniDir.resolve("komponente.txt").toString(), miniDir.resolve("veze.txt").toString());
    assertTrue(result.isValid(), result.getMessage());
  }

  @Test
  void validateRejectsMissingComponentsFile(@TempDir Path tempDir) {
    JobValidator validator = new JobValidator();
    ValidationResult result =
        validator.validate(
            tempDir.resolve("missing.txt").toString(), tempDir.resolve("veze.txt").toString());
    assertFalse(result.isValid());
    assertTrue(result.getMessage().contains("not found"));
  }

  @Test
  void validateRejectsEmptyComponents(@TempDir Path tempDir) throws Exception {
    Path components = tempDir.resolve("komponente.txt");
    Path connections = tempDir.resolve("veze.txt");
    java.nio.file.Files.writeString(components, "");
    java.nio.file.Files.writeString(connections, "srcID srcPort dstID dstPort\n");

    JobValidator validator = new JobValidator();
    ValidationResult result = validator.validate(components.toString(), connections.toString());
    assertFalse(result.isValid());
    assertTrue(result.getMessage().contains("no components"));
  }

  private static Path miniDir() throws Exception {
    URL resource = JobValidatorTest.class.getClassLoader().getResource("mini/komponente.txt");
    if (resource == null) {
      throw new IllegalStateException("mini test resources not found");
    }
    return Path.of(resource.toURI()).getParent();
  }
}
