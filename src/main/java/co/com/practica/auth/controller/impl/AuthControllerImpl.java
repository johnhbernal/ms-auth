package co.com.practica.auth.controller.impl;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.controller.AuthController;
import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.LoginRequest;
import co.com.practica.auth.dto.LoginResponse;
import co.com.practica.auth.dto.RenewTokenRequest;
import co.com.practica.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller that exposes authentication endpoints.
 *
 * <pre>
 * POST /api/auth/login     — Authenticate and receive a session token
 * POST /api/auth/renew     — Renew the session token
 * GET  /api/auth/validate  — Check whether a token is still valid
 * </pre>
 */
@Log4j2
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     *
     * <p>Request:  {@code { "username": "admin", "password": "Admin123!" }}
     * <p>Response: {@code { "code": "200", "data": { "sessionToken": "eyJ...", "role": "ADMIN", ... } }}
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        // Do NOT log the password — only the username
        log.info("POST /api/auth/login — username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * POST /api/auth/renew
     *
     * <p>Request:  {@code { "sessionToken": "eyJ..." }}
     * <p>Response: {@code { "code": "200", "data": { "sessionToken": "eyJ...(new)", ... } }}
     */
    @Override
    @PostMapping("/renew")
    public ResponseEntity<ApiResponse> renewToken(@Valid @RequestBody RenewTokenRequest request) {
        // Do NOT log the token value
        log.info("POST /api/auth/renew");
        LoginResponse response = authService.renewToken(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * GET /api/auth/validate?token=eyJ...
     *
     * <p>Response 200: {@code { "code": "200", "data": true }}
     * <p>Response 401: {@code { "code": "401", "description": "Invalid or expired token" }}
     */
    @Override
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse> validateToken(@RequestParam String token) {
        log.debug("GET /api/auth/validate");
        boolean valid = authService.isSessionTokenValid(token);
        if (valid) {
            return ResponseEntity.ok(ApiResponse.ok(true));
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(AppConstants.CODE_UNAUTHORIZED, AppConstants.MSG_TOKEN_INVALID));
    }
}
