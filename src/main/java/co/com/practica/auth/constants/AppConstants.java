package co.com.practica.auth.constants;

/**
 * Application-wide constants for ms-auth.
 *
 * <p>Centralizes all magic strings and numeric literals to prevent
 * SonarQube "magic number" and "duplicated string literal" violations.
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ── HTTP Headers ────────────────────────────────────────────────────────
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX         = "Bearer ";

    // ── Custom JWT Claims ────────────────────────────────────────────────────
    public static final String CLAIM_ROLE       = "role";
    public static final String CLAIM_UUID       = "uuid";
    public static final String CLAIM_FULL_NAME  = "fullName";
    public static final String CLAIM_EMAIL      = "email";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    // ── Token types ──────────────────────────────────────────────────────────
    public static final String TOKEN_TYPE_MASTER  = "MASTER";
    public static final String TOKEN_TYPE_SESSION = "SESSION";

    // ── Record status ────────────────────────────────────────────────────────
    public static final String STATUS_ACTIVE   = "A";
    public static final String STATUS_INACTIVE = "I";

    // ── Response codes ───────────────────────────────────────────────────────
    public static final String CODE_OK           = "200";
    public static final String CODE_CREATED      = "201";
    public static final String CODE_BAD_REQUEST  = "400";
    public static final String CODE_UNAUTHORIZED = "401";
    public static final String CODE_FORBIDDEN    = "403";
    public static final String CODE_NOT_FOUND    = "404";
    public static final String CODE_ERROR        = "500";

    // ── Response messages ────────────────────────────────────────────────────
    public static final String MSG_LOGIN_SUCCESS       = "Authentication successful";
    public static final String MSG_TOKEN_RENEWED       = "Session token renewed successfully";
    public static final String MSG_TOKEN_INVALID       = "Invalid or expired token";
    public static final String MSG_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String MSG_USER_INACTIVE       = "User account is inactive";
    public static final String MSG_USER_NOT_FOUND      = "User not found";

    // ── Master token parameter name prefix (ms-practica) ────────────────────
    public static final String MASTER_TOKEN_PARAM_PREFIX = "MASTER_TOKEN_";
}
