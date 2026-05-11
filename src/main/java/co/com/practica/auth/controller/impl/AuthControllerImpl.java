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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * REST controller that exposes authentication endpoints.
 *
 * <pre>
 * POST /api/auth/login     — Authenticate and receive a session token
 * POST /api/auth/renew     — Renew the session token
 * POST /api/auth/logout    — Revoke the current session token
 * GET  /api/auth/validate  — Check whether a token is still valid
 * </pre>
 */
@Log4j2
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login — username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Pragma", "no-cache")
                .body(ApiResponse.ok(response));
    }

    @Override
    @PostMapping("/renew")
    public ResponseEntity<ApiResponse> renewToken(@Valid @RequestBody RenewTokenRequest request) {
        log.info("POST /api/auth/renew");
        LoginResponse response = authService.renewToken(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Pragma", "no-cache")
                .body(ApiResponse.ok(response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest httpRequest) {
        log.info("POST /api/auth/logout");
        String authHeader = httpRequest.getHeader(AppConstants.AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
            authService.logout(authHeader.substring(AppConstants.BEARER_PREFIX.length()));
        }
        return ResponseEntity.ok(ApiResponse.ok(AppConstants.MSG_LOGOUT_SUCCESS));
    }

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
