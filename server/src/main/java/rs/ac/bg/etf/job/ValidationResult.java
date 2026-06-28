package rs.ac.bg.etf.job;

/** Outcome of validating job input files before scheduling. */
public final class ValidationResult {

  private final boolean valid;
  private final String message;

  private ValidationResult(boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }

  /**
   * @return successful validation result
   */
  public static ValidationResult success() {
    return new ValidationResult(true, "");
  }

  /**
   * @param message human-readable failure reason
   * @return failed validation result
   */
  public static ValidationResult failure(String message) {
    return new ValidationResult(false, message);
  }

  /**
   * @return {@code true} when input files form a valid netlist
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * @return validation message (empty on success)
   */
  public String getMessage() {
    return message;
  }
}
