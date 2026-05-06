package co.com.practica.auth.controller;

import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;

/**
 * User management API — role-gated endpoints.
 *
 * <pre>
 * GET /api/users     — ADMIN only — list all users (no password hashes)
 * GET /api/users/me  — any authenticated user — current caller's claims
 * </pre>
 */
@Tag(name = "Users", description = "User listing and profile endpoints")
public interface UserController {

    @Operation(
        summary     = "Register a new user",
        description = "Creates a new user account. Requires ADMIN role. Role defaults to USER if omitted."
    )
    ResponseEntity<ApiResponse> register(@Valid RegisterRequest request);

    @Operation(
        summary     = "List all users",
        description = "Returns all registered users without password hashes. Requires ADMIN role."
    )
    ResponseEntity<ApiResponse> listUsers();

    @Operation(
        summary     = "Current user info",
        description = "Returns the caller's username, role, fullName and email extracted from the JWT. No DB call."
    )
    ResponseEntity<ApiResponse> me();
}
