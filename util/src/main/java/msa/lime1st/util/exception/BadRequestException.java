package msa.lime1st.util.exception;

public class BadRequestException extends RuntimeException {
  public BadRequestException() {}

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public BadRequestException(Throwable cause) {
    super(cause);
  }
}

