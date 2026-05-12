package co.com.practica.auth.service.impl;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.LoginRequest;
import co.com.practica.auth.dto.LoginResponse;
import co.com.practica.auth.dto.ParameterFeignDto;
import co.com.practica.auth.dto.RenewTokenRequest;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.exception.AuthException;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.AuthService;
import co.com.practica.auth.util.JwtUtil;
import co.com.practica.auth.util.PracticaServiceClient;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Default implementation of {@link AuthService}.
 *
 * <h3>Login flow</h3>
 * <ol>
 *   <li>Find active user by username.</li>
 *   <li>Verify password with BCrypt.</li>
 *   <li>Generate masterToken (24 h) and persist to User entity.</li>
 *   <li>Call ms-practica via Feign to store masterToken in PARAMETERS table.</li>
 *   <li>Generate new session UUID.</li>
 *   <li>Generate sessionToken (15 min) with UUID and user claims.</li>
 *   <li>Persist sessionToken, sessionUuid and lastLoginAt.</li>
 *   <li>Return {@link LoginResponse} with sessionToken to the caller.</li>
 * </ol>
 *
 * <h3>Renewal flow</h3>
 * <ol>
 *   <li>Extract session UUID from submitted sessionToken claims.</li>
 *   <li>Find user by that session UUID.</li>
 *   <li>Verify user is still active.</li>
 *   <li>Rotate UUID — generate a new one (previous session is now invalid).</li>
 *   <li>Generate new sessionToken (15 min) with the new UUID.</li>
 *   <li>Persist updated sessionToken, sessionUuid and expiry.</li>
 *   <li>Return {@link LoginResponse} with the new sessionToken.</li>
 * </ol>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final JwtUtil               jwtUtil;
    private final PasswordEncoder       passwordEncoder;
    private final PracticaServiceClient practicaServiceClient;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt");

        User user = findActiveUser(request.getUsername());

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Login rejected — account locked");
            throw new AuthException(AppConstants.MSG_ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= AppConstants.MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(AppConstants.LOCKOUT_DURATION_MINS));
                log.warn("Account locked after {} failed attempts", attempts);
            }
            userRepository.save(user);
            log.warn("Authentication failed — invalid credentials");
            throw new AuthException(AppConstants.MSG_INVALID_CREDENTIALS);
        }

        // Reset failed attempts on successful authentication
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        String masterToken = jwtUtil.generateMasterToken(user.getUsername());
        user.setMasterToken(hashToken(masterToken));
        storeMasterTokenInPractica(user, masterToken);

        String sessionUuid  = jwtUtil.generateSessionUuid();
        String sessionToken = jwtUtil.generateSessionToken(user, sessionUuid);

        LocalDateTime now = LocalDateTime.now();
        user.setSessionToken(sessionToken);
        user.setSessionUuid(sessionUuid);
        user.setLastLoginAt(now);
        user.setSessionTokenExpiresAt(now.plusMinutes(AppConstants.SESSION_EXPIRATION_MINS));
        userRepository.save(user);

        log.info("Login successful — username: {} role: {}", user.getUsername(), user.getRole());
        return buildLoginResponse(user, sessionToken, AppConstants.MSG_LOGIN_SUCCESS);
    }

    @Override
    @Transactional
    public LoginResponse renewToken(RenewTokenRequest request) {
        log.info("Token renewal requested");

        Claims claims = extractClaimsSafely(request.getSessionToken());

        String currentUuid = claims.get(AppConstants.CLAIM_UUID, String.class);
        if (currentUuid == null || currentUuid.isEmpty()) {
            throw new AuthException(AppConstants.MSG_TOKEN_INVALID);
        }

        User user = userRepository
                .findBySessionUuid(currentUuid)
                .orElseThrow(() -> new AuthException(AppConstants.MSG_TOKEN_INVALID));

        if (!AppConstants.STATUS_ACTIVE.equals(user.getStatus())) {
            throw new AuthException(AppConstants.MSG_USER_INACTIVE);
        }

        String newUuid         = jwtUtil.generateSessionUuid();
        String newSessionToken = jwtUtil.generateSessionToken(user, newUuid);

        LocalDateTime now = LocalDateTime.now();
        user.setSessionToken(newSessionToken);
        user.setSessionUuid(newUuid);
        user.setSessionTokenExpiresAt(now.plusMinutes(AppConstants.SESSION_EXPIRATION_MINS));
        userRepository.save(user);

        log.info("Token renewed for username: {}", user.getUsername());
        return buildLoginResponse(user, newSessionToken, AppConstants.MSG_TOKEN_RENEWED);
    }

    @Override
    public boolean isSessionTokenValid(String token) {
        if (!jwtUtil.isSessionTokenValid(token)) return false;
        try {
            String uuid = jwtUtil.extractSessionClaims(token)
                                 .get(AppConstants.CLAIM_UUID, String.class);
            return uuid != null && userRepository.findBySessionUuid(uuid).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void logout(String sessionToken) {
        try {
            String uuid = jwtUtil.extractSessionClaims(sessionToken)
                                 .get(AppConstants.CLAIM_UUID, String.class);
            if (uuid == null) return;
            userRepository.findBySessionUuid(uuid).ifPresent(user -> {
                user.setSessionToken(null);
                user.setSessionUuid(null);
                userRepository.save(user);
                log.info("Session invalidated for user: {}", user.getUsername());
            });
        } catch (Exception e) {
            log.warn("Logout: could not extract session claims — {}", e.getMessage());
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Finds an active user by username, throwing {@link AuthException} with a
     * generic message on failure (prevents user enumeration attacks).
     */
    private User findActiveUser(String username) {
        return userRepository
                .findByUsernameAndStatus(username, AppConstants.STATUS_ACTIVE)
                .orElseThrow(() -> new AuthException(AppConstants.MSG_INVALID_CREDENTIALS));
    }

    /**
     * Extracts JWT claims from a session token.
     * Accepts expired tokens so renewal can proceed after expiry.
     *
     * @throws AuthException if the token is malformed or has an invalid signature.
     */
    private Claims extractClaimsSafely(String sessionToken) {
        try {
            return jwtUtil.extractSessionClaims(sessionToken);
        } catch (Exception e) {
            log.warn("Failed to extract claims from session token: {}", e.getMessage());
            throw new AuthException(AppConstants.MSG_TOKEN_INVALID);
        }
    }

    /**
     * Builds the {@link LoginResponse} from the given user and token.
     * Centralizes response construction to avoid duplication between login and renewal.
     */
    private LoginResponse buildLoginResponse(User user, String sessionToken, String message) {
        return LoginResponse.builder()
                .sessionToken(sessionToken)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresAtMs(System.currentTimeMillis() + jwtUtil.getSessionExpirationMs())
                .message(message)
                .build();
    }

    /**
     * Calls ms-practica via Feign to store the master token in the PARAMETERS table.
     * Parameter name pattern: {@code MASTER_TOKEN_<USERNAME>}
     *
     * <p>Login continues even if ms-practica is unreachable — failure is only logged,
     * not propagated, to avoid coupling authentication availability to ms-practica.
     */
    private void storeMasterTokenInPractica(User user, String masterToken) {
        try {
            ParameterFeignDto parameter = ParameterFeignDto.builder()
                    .parameterName(AppConstants.MASTER_TOKEN_PARAM_PREFIX + user.getUsername().toUpperCase())
                    .parameterValue(hashToken(masterToken))
                    .parameterDescription("Master session token for user: " + user.getUsername())
                    .status(AppConstants.STATUS_ACTIVE)
                    .build();

            practicaServiceClient.createParameter(AppConstants.BEARER_PREFIX + masterToken, parameter);
            log.info("Master token stored in ms-practica for username: {}", user.getUsername());

        } catch (Exception e) {
            log.error("Could not store master token in ms-practica for {}: {}",
                    user.getUsername(), e.getMessage());
        }
    }

    private static String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
