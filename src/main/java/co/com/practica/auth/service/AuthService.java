package co.com.practica.auth.service;

import co.com.practica.auth.dto.LoginRequest;
import co.com.practica.auth.dto.LoginResponse;
import co.com.practica.auth.dto.RenewTokenRequest;

/**
 * Authentication service contract.
 *
 * <p>Implementations handle credential validation, JWT generation,
 * session lifecycle management and integration with ms-practica.
 */
public interface AuthService {

    /**
     * Authenticates a user and issues a session token.
     *
     * @param request login credentials
     * @return {@link LoginResponse} containing the session token and user info
     */
    LoginResponse login(LoginRequest request);

    /**
     * Renews an existing session token.
     * The previous session UUID is rotated, invalidating the old token.
     *
     * @param request current session token
     * @return {@link LoginResponse} containing the new session token
     */
    LoginResponse renewToken(RenewTokenRequest request);

    /**
     * Validates whether a session token is syntactically correct and not expired.
     *
     * @param token session JWT to validate
     * @return {@code true} if valid; {@code false} otherwise
     */
    boolean isSessionTokenValid(String token);
}
