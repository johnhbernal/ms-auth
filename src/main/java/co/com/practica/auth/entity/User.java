package co.com.practica.auth.entity;

import co.com.practica.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity mapped to the {@code USERS} table.
 *
 * <h3>Key fields</h3>
 * <ul>
 *   <li><b>masterToken</b>  — SHA-256 (Base64) hash of the long-lived master JWT
 *       (24 h); also stored hashed in ms-practica PARAMETERS via Feign.</li>
 *   <li><b>sessionToken</b> — SHA-256 (Base64) hash of the short-lived session JWT
 *       (15 min). The plaintext JWT is returned to the client only; this column
 *       never stores the raw JWT.</li>
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
     * SHA-256 (Base64) hash of the master JWT (24 h).
     * Stored here AND (hashed) in ms-practica PARAMETERS table.
     */
    @Column(name = "MASTER_TOKEN", columnDefinition = "TEXT")
    private String masterToken;

    /**
     * SHA-256 (Base64) hash of the session JWT (15 min) — not the JWT itself.
     * The client receives the plaintext JWT once and sends it as
     * {@code Authorization: Bearer <sessionToken>}; revocation uses {@code sessionUuid}.
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

    /** Consecutive failed login attempts since last successful login. */
    @Column(name = "FAILED_LOGIN_ATTEMPTS", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    /** When set, the account is locked until this timestamp. */
    @Column(name = "LOCKED_UNTIL")
    private LocalDateTime lockedUntil;

    /**
     * Simulated AD group membership ({@code memberOf}).
     * Groups grant {@link AppRole}s → {@link Permission}s at login.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "USER_GROUPS",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<DirectoryGroup> directoryGroups = new HashSet<>();

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
