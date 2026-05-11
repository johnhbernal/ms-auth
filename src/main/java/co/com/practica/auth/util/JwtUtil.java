package co.com.practica.auth.util;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utility component for JWT generation, validation and claims extraction.
 *
 * <p>Two separate signing keys are used:
 * <ul>
 *   <li><b>masterKey</b>  — signs long-lived master tokens (24 h).</li>
 *   <li><b>sessionKey</b> — signs short-lived session tokens (15 min).</li>
 * </ul>
 */
@Log4j2
@Component
public class JwtUtil {

    @Value("${app.jwt.secret-master}")
    private String masterSecret;

    @Value("${app.jwt.secret-session}")
    private String sessionSecret;

    @Value("${app.jwt.master-expiration-ms:86400000}")
    private long masterExpirationMs;

    @Value("${app.jwt.session-expiration-ms:900000}")
    private long sessionExpirationMs;

    private SecretKey masterKey;
    private SecretKey sessionKey;

    @PostConstruct
    public void init() {
        this.masterKey  = Keys.hmacShaKeyFor(masterSecret.getBytes(StandardCharsets.UTF_8));
        this.sessionKey = Keys.hmacShaKeyFor(sessionSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtUtil initialized — masterExpiration={}ms, sessionExpiration={}ms",
                masterExpirationMs, sessionExpirationMs);
    }

    /**
     * Generates a master token (24 h) for the given username.
     *
     * @param username the authenticated user's username
     * @return signed master JWT
     */
    public String generateMasterToken(String username) {
        Date now        = new Date();
        Date expiration = new Date(now.getTime() + masterExpirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim(AppConstants.CLAIM_TOKEN_TYPE, AppConstants.TOKEN_TYPE_MASTER)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(masterKey)
                .compact();
    }

    /**
     * Validates a master token.
     *
     * @param token the master JWT to validate
     * @return {@code true} if valid and not expired; {@code false} otherwise
     */
    public boolean isMasterTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(masterKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid master token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generates a session token (15 min) with user claims and a session UUID.
     *
     * @param user        the authenticated user
     * @param sessionUuid unique identifier for this session
     * @return signed session JWT
     */
    public String generateSessionToken(User user, String sessionUuid) {
        Date now        = new Date();
        Date expiration = new Date(now.getTime() + sessionExpirationMs);
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim(AppConstants.CLAIM_UUID,       sessionUuid)
                .claim(AppConstants.CLAIM_FULL_NAME,  user.getFullName())
                .claim(AppConstants.CLAIM_EMAIL,      user.getEmail())
                .claim(AppConstants.CLAIM_ROLE,       user.getRole().name())
                .claim(AppConstants.CLAIM_TOKEN_TYPE, AppConstants.TOKEN_TYPE_SESSION)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(sessionKey)
                .compact();
    }

    /**
     * Generates a random UUID to identify a session.
     *
     * @return UUID string
     */
    public String generateSessionUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Validates a session token.
     *
     * @param token the session JWT to validate
     * @return {@code true} if valid and not expired; {@code false} otherwise
     */
    public boolean isSessionTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(sessionKey).build()
                    .parseClaimsJws(token).getBody();
            String tokenType = claims.get(AppConstants.CLAIM_TOKEN_TYPE, String.class);
            if (!AppConstants.TOKEN_TYPE_SESSION.equals(tokenType)) {
                log.warn("Token type mismatch: expected SESSION");
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Session token expired");
            return false;
        } catch (Exception e) {
            log.warn("Invalid session token");
            return false;
        }
    }

    /**
     * Extracts claims from a session token.
     *
     * <p>Unlike {@link #isSessionTokenValid}, this method also returns claims from
     * <em>expired</em> tokens so that renewal can proceed after the 15-min window.
     *
     * @param token session JWT (may be expired)
     * @return parsed {@link Claims}
     */
    public Claims extractSessionClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(sessionKey).build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Intentionally return claims even for expired tokens (renewal use case)
            return e.getClaims();
        }
    }

    /**
     * Extracts the username (subject) from a session token.
     *
     * @param token session JWT
     * @return username string
     */
    public String extractUsernameFromSession(String token) {
        return extractSessionClaims(token).getSubject();
    }

    /**
     * Returns the configured session token expiration in milliseconds.
     *
     * @return session expiration in ms
     */
    public long getSessionExpirationMs() {
        return sessionExpirationMs;
    }
}
