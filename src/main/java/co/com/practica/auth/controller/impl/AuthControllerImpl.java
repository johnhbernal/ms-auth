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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * REST controller that exposes authentication endpoints.
 *
 * <pre>
 * POST /api/auth/login     — Authenticate and receive a session token
 * POST /api/auth/renew     — Renew the session token
 * POST /api/auth/logout    — Revoke the current session token (Bearer required)
 * GET  /api/auth/validate  — Check whether a token is still valid
 * </pre>
 */
@Log4j2
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @Override
    public ResponseEntity<ApiResponse> login(@Valid LoginRequest request) {
        log.info("POST /api/auth/login — username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Pragma", "no-cache")
                .body(ApiResponse.ok(response));
    }

    @Override
    public ResponseEntity<ApiResponse> renewToken(@Valid RenewTokenRequest request) {
        log.info("POST /api/auth/renew");
        LoginResponse response = authService.renewToken(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Pragma", "no-cache")
                .body(ApiResponse.ok(response));
    }

    @Override
    public ResponseEntity<ApiResponse> logout(HttpServletRequest httpRequest) {
        log.info("POST /api/auth/logout");
        String authHeader = httpRequest.getHeader(AppConstants.AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
            authService.logout(authHeader.substring(AppConstants.BEARER_PREFIX.length()));
        }
        return ResponseEntity.ok(ApiResponse.ok(AppConstants.MSG_LOGOUT_SUCCESS));
    }

    @Override
    public ResponseEntity<ApiResponse> validateToken(String authorization, String token) {
        log.debug("GET /api/auth/validate");
        String resolved = resolveToken(authorization, token);
        if (resolved == null || resolved.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(AppConstants.CODE_BAD_REQUEST,
                            "Token is required (Authorization: Bearer or token query param)"));
        }
        boolean valid = authService.isSessionTokenValid(resolved);
        if (valid) {
            return ResponseEntity.ok(ApiResponse.ok(true));
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(AppConstants.CODE_UNAUTHORIZED, AppConstants.MSG_TOKEN_INVALID));
    }

    /**
     * Prefer Authorization header (OWASP A04 — sensitive data not in URL/logs).
     */
    static String resolveToken(String authorization, String token) {
        if (authorization != null && authorization.startsWith(AppConstants.BEARER_PREFIX)) {
            return authorization.substring(AppConstants.BEARER_PREFIX.length()).trim();
        }
        if (token != null && !token.isBlank()) {
            return token.trim();
        }
        return null;
    }
}
