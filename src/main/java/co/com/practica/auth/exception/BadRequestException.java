package co.com.practica.auth.exception;

/** Client error (400) for invalid business input such as expired reset tokens. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
