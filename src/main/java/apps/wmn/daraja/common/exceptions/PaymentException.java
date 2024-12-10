package apps.wmn.daraja.common.exceptions;

public class PaymentException extends RuntimeException {
  public PaymentException(String message) {
    super(message);
  }

  public PaymentException(String message, Throwable cause) {
    super(message, cause);
  }
}
