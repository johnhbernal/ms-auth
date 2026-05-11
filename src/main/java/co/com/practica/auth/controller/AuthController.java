package co.com.practica.auth.controller;

import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.LoginRequest;
import co.com.practica.auth.dto.RenewTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Authentication API contract.
 *
 * <p>All endpoints are public (no JWT required) — see {@code SecurityConfig}.
 */
@Tag(name = "Authentication", description = "Login, token renewal and validation endpoints")
public interface AuthController {

    @Operation(
        summary = "User login",
        description = "Authenticates the user, generates a master token (24 h) stored in ms-practica, " +
                      "and returns a session token (15 min) with embedded UUID, fullName, email and role."
    )
    ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(
        summary = "Renew session token",
        description = "Generates a new session token (15 min) with a rotated UUID. " +
                      "The previous token is immediately invalidated. " +
                      "Works even if the current token has already expired."
    )
    ResponseEntity<ApiResponse> renewToken(@Valid @RequestBody RenewTokenRequest request);

    @Operation(
        summary = "Validate session token",
        description = "Returns true if the session token is valid, not expired, and not logged out."
    )
    ResponseEntity<ApiResponse> validateToken(
            @Parameter(description = "Session JWT to validate", required = true)
            @RequestParam String token
    );

    @Operation(
        summary = "Logout",
        description = "Revokes the current session token. Subsequent validate calls will return false."
    )
    ResponseEntity<ApiResponse> logout(HttpServletRequest request);
}
