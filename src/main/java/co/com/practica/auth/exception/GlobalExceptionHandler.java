package co.com.practica.auth.exception;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.ApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler. Converts exceptions into structured JSON responses.
 *
 * <p>Handles:
 * <ul>
 *   <li>{@link AuthException}                        → 401 Unauthorized</li>
 *   <li>{@link ResourceNotFoundException}            → 404 Not Found</li>
 *   <li>{@link MissingServletRequestParameterException} → 400 Bad Request</li>
 *   <li>{@link MethodArgumentNotValidException}      → 400 Bad Request</li>
 *   <li>{@link Exception}                            → 500 Internal Server Error</li>
 * </ul>
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse> handleAuthException(AuthException ex) {
        log.warn("AuthException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(AppConstants.CODE_UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFoundException(ResourceNotFoundException ex) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(AppConstants.CODE_NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(AppConstants.CODE_BAD_REQUEST, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(AppConstants.CODE_BAD_REQUEST, errors));
    }

    // @PreAuthorize throws AccessDeniedException inside the MVC layer (not the filter chain),
    // so JwtAccessDeniedHandler never sees it — this handler bridges the gap.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(AppConstants.CODE_FORBIDDEN, AppConstants.MSG_FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(AppConstants.CODE_ERROR, "Internal server error"));
    }
}
