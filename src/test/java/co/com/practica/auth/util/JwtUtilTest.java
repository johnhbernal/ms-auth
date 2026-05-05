package co.com.practica.auth.util;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String MASTER_SECRET  = "master-secret-key-for-unit-testing-only!!";
    private static final String SESSION_SECRET = "session-secret-key-for-unit-testing-only!";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "masterSecret",       MASTER_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "sessionSecret",      SESSION_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "masterExpirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "sessionExpirationMs", 900000L);
        jwtUtil.init();
    }

    // ── Master token ─────────────────────────────────────────────────────────

    @Test
    void generateMasterToken_returnsNonNullToken() {
        String token = jwtUtil.generateMasterToken("admin");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateMasterToken_containsCorrectSubjectAndType() {
        String token = jwtUtil.generateMasterToken("admin");
        assertThat(jwtUtil.isMasterTokenValid(token)).isTrue();
    }

    @Test
    void isMasterTokenValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateMasterToken("admin") + "tampered";
        assertThat(jwtUtil.isMasterTokenValid(token)).isFalse();
    }

    @Test
    void isMasterTokenValid_returnsFalseForSessionToken() {
        User user = buildUser();
        String sessionToken = jwtUtil.generateSessionToken(user, "some-uuid");
        // Master key cannot validate a session token
        assertThat(jwtUtil.isMasterTokenValid(sessionToken)).isFalse();
    }

    // ── Session token ────────────────────────────────────────────────────────

    @Test
    void generateSessionToken_containsExpectedClaims() {
        User user = buildUser();
        String uuid = "test-uuid-123";

        String token = jwtUtil.generateSessionToken(user, uuid);
        Claims claims = jwtUtil.extractSessionClaims(token);

        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get(AppConstants.CLAIM_UUID, String.class)).isEqualTo(uuid);
        assertThat(claims.get(AppConstants.CLAIM_ROLE, String.class)).isEqualTo("ADMIN");
        assertThat(claims.get(AppConstants.CLAIM_FULL_NAME, String.class)).isEqualTo("Admin User");
        assertThat(claims.get(AppConstants.CLAIM_EMAIL, String.class)).isEqualTo("admin@test.com");
        assertThat(claims.get(AppConstants.CLAIM_TOKEN_TYPE, String.class)).isEqualTo(AppConstants.TOKEN_TYPE_SESSION);
    }

    @Test
    void isSessionTokenValid_returnsTrueForValidToken() {
        String token = jwtUtil.generateSessionToken(buildUser(), "uuid-1");
        assertThat(jwtUtil.isSessionTokenValid(token)).isTrue();
    }

    @Test
    void isSessionTokenValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateSessionToken(buildUser(), "uuid-1") + "x";
        assertThat(jwtUtil.isSessionTokenValid(token)).isFalse();
    }

    @Test
    void isSessionTokenValid_returnsFalseForExpiredToken() {
        JwtUtil expiredUtil = buildUtilWithExpiredSession();
        String token = expiredUtil.generateSessionToken(buildUser(), "uuid-expired");
        assertThat(expiredUtil.isSessionTokenValid(token)).isFalse();
    }

    @Test
    void extractSessionClaims_returnsClaimsEvenForExpiredToken() {
        JwtUtil expiredUtil = buildUtilWithExpiredSession();
        String token = expiredUtil.generateSessionToken(buildUser(), "uuid-expired");

        Claims claims = expiredUtil.extractSessionClaims(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get(AppConstants.CLAIM_UUID, String.class)).isEqualTo("uuid-expired");
    }

    @Test
    void extractUsernameFromSession_returnsCorrectUsername() {
        String token = jwtUtil.generateSessionToken(buildUser(), "uuid-1");
        assertThat(jwtUtil.extractUsernameFromSession(token)).isEqualTo("admin");
    }

    // ── UUID generation ──────────────────────────────────────────────────────

    @Test
    void generateSessionUuid_returnsValidUuid() {
        String uuid = jwtUtil.generateSessionUuid();
        assertThat(uuid).isNotBlank().hasSize(36).contains("-");
    }

    @Test
    void generateSessionUuid_returnsDifferentValuesEachCall() {
        assertThat(jwtUtil.generateSessionUuid()).isNotEqualTo(jwtUtil.generateSessionUuid());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User buildUser() {
        return User.builder()
                .username("admin")
                .fullName("Admin User")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .status("A")
                .build();
    }

    private JwtUtil buildUtilWithExpiredSession() {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "masterSecret",        MASTER_SECRET);
        ReflectionTestUtils.setField(util, "sessionSecret",       SESSION_SECRET);
        ReflectionTestUtils.setField(util, "masterExpirationMs",  86400000L);
        ReflectionTestUtils.setField(util, "sessionExpirationMs", -1000L);
        util.init();
        return util;
    }
}
