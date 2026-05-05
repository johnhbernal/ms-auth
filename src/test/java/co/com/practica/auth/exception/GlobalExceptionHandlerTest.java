package co.com.practica.auth.exception;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAuthException_returns401WithMessage() {
        AuthException ex = new AuthException("Invalid credentials");

        ResponseEntity<ApiResponse> response = handler.handleAuthException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(AppConstants.CODE_UNAUTHORIZED);
        assertThat(response.getBody().getDescription()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleNotFoundException_returns404WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        ResponseEntity<ApiResponse> response = handler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(AppConstants.CODE_NOT_FOUND);
        assertThat(response.getBody().getDescription()).isEqualTo("User not found");
    }

    @Test
    void handleMissingParameter_returns400WithParameterName() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("token", "String");

        ResponseEntity<ApiResponse> response = handler.handleMissingParameter(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(AppConstants.CODE_BAD_REQUEST);
        assertThat(response.getBody().getDescription()).contains("token");
    }

    @Test
    void handleValidationException_returns400WithFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "loginRequest");
        bindingResult.addError(new FieldError("loginRequest", "username", "Username is required"));

        MethodParameter methodParameter = new MethodParameter(String.class.getMethod("toString"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiResponse> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(AppConstants.CODE_BAD_REQUEST);
        assertThat(response.getBody().getDescription()).contains("username").contains("Username is required");
    }

    @Test
    void handleGenericException_returns500WithGenericMessage() {
        Exception ex = new RuntimeException("Something broke");

        ResponseEntity<ApiResponse> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(AppConstants.CODE_ERROR);
        assertThat(response.getBody().getDescription()).isEqualTo("Internal server error");
    }
}
