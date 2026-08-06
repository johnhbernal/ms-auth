package co.com.practica.auth.controller;

import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.LoginRequest;
import co.com.practica.auth.dto.RenewTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Authentication API contract.
 *
 * <p>Mappings live on the interface so Spring MVC + IDE (IntelliJ/Cursor) treat
 * {@code renewToken}/{@code validateToken} as entry points (not “unused”).
 *
 * <p>Login/renew/validate are public; logout requires a Bearer session token.
 */
@Tag(name = "Authentication", description = "Login, token renewal and validation endpoints")
public interface AuthController {

    @Operation(
        summary = "User login",
        description = "Authenticates the user, generates a master token (24 h) stored in ms-practica, " +
                      "and returns a session token (15 min) with embedded UUID, fullName, email and role."
    )
    @PostMapping("/login")
    ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(
        summary = "Renew session token",
        description = "Generates a new session token (15 min) with a rotated UUID. " +
                      "The previous token is immediately invalidated. " +
                      "Works even if the current token has already expired."
    )
    @PostMapping("/renew")
    ResponseEntity<ApiResponse> renewToken(@Valid @RequestBody RenewTokenRequest request);

    @Operation(
        summary = "Validate session token",
        description = "Prefer Authorization: Bearer (OWASP — avoid JWT in query strings/logs). " +
                      "Optional ?token= kept for backward compatibility."
    )
    @GetMapping("/validate")
    ResponseEntity<ApiResponse> validateToken(
            @Parameter(description = "Bearer session JWT (preferred)")
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "Deprecated: JWT in query string — prefer Authorization header")
            @RequestParam(value = "token", required = false) String token
    );

    @Operation(
        summary = "Logout",
        description = "Revokes the current session token. Requires Authorization: Bearer."
    )
    @PostMapping("/logout")
    ResponseEntity<ApiResponse> logout(HttpServletRequest request);
}
