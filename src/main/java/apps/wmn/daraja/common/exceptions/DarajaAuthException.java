package apps.wmn.daraja.common.exceptions;

public class DarajaAuthException extends RuntimeException {
  public DarajaAuthException(String message) {
    super(message);
  }

  public DarajaAuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
