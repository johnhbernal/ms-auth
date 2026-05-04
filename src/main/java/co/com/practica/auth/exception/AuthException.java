package co.com.practica.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when authentication fails (invalid credentials, inactive user,
 * invalid or expired token).
 *
 * <p>Mapped to HTTP 401 Unauthorized by {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }
}
