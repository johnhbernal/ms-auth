package co.com.practica.auth.entity;

import co.com.practica.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity mapped to the {@code USERS} table.
 *
 * <h3>Key fields</h3>
 * <ul>
 *   <li><b>masterToken</b>  — Long-lived JWT (24 h) stored here and in
 *       ms-practica PARAMETERS table via Feign.</li>
 *   <li><b>sessionToken</b> — Short-lived JWT (15 min) with UUID, user data
 *       and role. This is the token the client uses in every request.</li>
 *   <li><b>sessionUuid</b>  — Unique UUID per active session. Rotated on every
 *       renewal, which immediately invalidates the previous session token.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false)
    private String passwordHash;

    @Column(name = "FULL_NAME", nullable = false, length = 100)
    private String fullName;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * User role: ADMIN, USER or READONLY.
     * Stored as String for readability and safe schema evolution.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private Role role;

    /**
     * Master JWT token (24 h).
     * Stored here AND in ms-practica PARAMETERS table.
     */
    @Column(name = "MASTER_TOKEN", columnDefinition = "TEXT")
    private String masterToken;

    /**
     * Session JWT token (15 min).
     * The client uses this token in every request:
     * {@code Authorization: Bearer <sessionToken>}
     */
    @Column(name = "SESSION_TOKEN", columnDefinition = "TEXT")
    private String sessionToken;

    /**
     * Unique UUID per active session.
     * Embedded as a claim inside the session token.
     * Rotating this UUID invalidates the previous session token.
     */
    @Column(name = "SESSION_UUID", length = 36)
    private String sessionUuid;

    /** {@code A} = Active, {@code I} = Inactive. */
    @Column(name = "STATUS", nullable = false, length = 1)
    @Builder.Default
    private String status = "A";

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @Column(name = "SESSION_TOKEN_EXPIRES_AT")
    private LocalDateTime sessionTokenExpiresAt;

    /**
     * Sets {@code createdAt} automatically before the first persist,
     * avoiding reliance on {@code @Builder.Default} for temporal fields.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
