package co.com.practica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned after a successful login or token renewal.
 *
 * <p>The {@code sessionToken} (15 min JWT) contains the session UUID as an
 * embedded claim. The client does NOT need the UUID separately — it sends
 * the {@code sessionToken} directly to {@code POST /api/auth/renew} when
 * renewal is needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Session JWT (15 min).
     * Use in every request: {@code Authorization: Bearer <sessionToken>}
     * To renew, send this token to {@code POST /api/auth/renew}.
     */
    private String sessionToken;

    /** User's full name. */
    private String fullName;

    /** User's email address. */
    private String email;

    /** Assigned role: ADMIN, USER or READONLY. */
    private String role;

    /** Token expiration timestamp in milliseconds since epoch. */
    private Long expiresAtMs;

    /** Informational message for the client. */
    private String message;
}
