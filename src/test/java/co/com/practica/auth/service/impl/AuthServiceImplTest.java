package co.com.practica.auth.service.impl;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.LoginRequest;
import co.com.practica.auth.dto.LoginResponse;
import co.com.practica.auth.dto.RenewTokenRequest;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import co.com.practica.auth.exception.AuthException;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.util.JwtUtil;
import co.com.practica.auth.util.PracticaServiceClient;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository        userRepository;
    @Mock private JwtUtil               jwtUtil;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private PracticaServiceClient practicaServiceClient;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .username("admin")
                .passwordHash("$2a$10$hashed")
                .fullName("Admin User")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .status(AppConstants.STATUS_ACTIVE)
                .build();
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void login_happyPath_returnsLoginResponse() {
        LoginRequest request = new LoginRequest("admin", "Admin123!");

        when(userRepository.findByUsernameAndStatus("admin", AppConstants.STATUS_ACTIVE))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Admin123!", "$2a$10$hashed")).thenReturn(true);
        when(jwtUtil.generateMasterToken("admin")).thenReturn("master-token");
        when(jwtUtil.generateSessionUuid()).thenReturn("uuid-1");
        when(jwtUtil.generateSessionToken(any(), anyString())).thenReturn("session-token");
        when(jwtUtil.getSessionExpirationMs()).thenReturn(900000L);
        when(userRepository.save(any())).thenReturn(activeUser);

        LoginResponse response = authService.login(request);

        assertThat(response.getSessionToken()).isEqualTo("session-token");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getMessage()).isEqualTo(AppConstants.MSG_LOGIN_SUCCESS);
        verify(userRepository).save(activeUser);
    }

    @Test
    void login_wrongPassword_throwsAuthException() {
        LoginRequest request = new LoginRequest("admin", "wrong");

        when(userRepository.findByUsernameAndStatus("admin", AppConstants.STATUS_ACTIVE))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "$2a$10$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.MSG_INVALID_CREDENTIALS);
    }

    @Test
    void login_userNotFound_throwsAuthException() {
        LoginRequest request = new LoginRequest("ghost", "pass");

        when(userRepository.findByUsernameAndStatus("ghost", AppConstants.STATUS_ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.MSG_INVALID_CREDENTIALS);
    }

    @Test
    void login_feignFailure_doesNotPropagateException() {
        LoginRequest request = new LoginRequest("admin", "Admin123!");

        when(userRepository.findByUsernameAndStatus("admin", AppConstants.STATUS_ACTIVE))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateMasterToken(anyString())).thenReturn("master-token");
        when(jwtUtil.generateSessionUuid()).thenReturn("uuid-1");
        when(jwtUtil.generateSessionToken(any(), anyString())).thenReturn("session-token");
        when(jwtUtil.getSessionExpirationMs()).thenReturn(900000L);
        when(userRepository.save(any())).thenReturn(activeUser);
        doThrow(new RuntimeException("Feign error"))
                .when(practicaServiceClient).createParameter(anyString(), any());

        // Login must succeed even when ms-practica is unreachable
        LoginResponse response = authService.login(request);
        assertThat(response.getSessionToken()).isEqualTo("session-token");
    }

    // ── renewToken ───────────────────────────────────────────────────────────

    @Test
    void renewToken_happyPath_returnsNewSessionToken() {
        Claims claims = mock(Claims.class);
        when(claims.get(AppConstants.CLAIM_UUID, String.class)).thenReturn("old-uuid");

        when(jwtUtil.extractSessionClaims("old-token")).thenReturn(claims);
        when(userRepository.findBySessionUuid("old-uuid")).thenReturn(Optional.of(activeUser));
        when(jwtUtil.generateSessionUuid()).thenReturn("new-uuid");
        when(jwtUtil.generateSessionToken(any(), anyString())).thenReturn("new-session-token");
        when(jwtUtil.getSessionExpirationMs()).thenReturn(900000L);
        when(userRepository.save(any())).thenReturn(activeUser);

        LoginResponse response = authService.renewToken(new RenewTokenRequest("old-token"));

        assertThat(response.getSessionToken()).isEqualTo("new-session-token");
        assertThat(response.getMessage()).isEqualTo(AppConstants.MSG_TOKEN_RENEWED);
    }

    @Test
    void renewToken_missingUuidInClaims_throwsAuthException() {
        Claims claims = mock(Claims.class);
        when(claims.get(AppConstants.CLAIM_UUID, String.class)).thenReturn(null);
        when(jwtUtil.extractSessionClaims("bad-token")).thenReturn(claims);

        assertThatThrownBy(() -> authService.renewToken(new RenewTokenRequest("bad-token")))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.MSG_TOKEN_INVALID);
    }

    @Test
    void renewToken_sessionUuidNotFound_throwsAuthException() {
        Claims claims = mock(Claims.class);
        when(claims.get(AppConstants.CLAIM_UUID, String.class)).thenReturn("unknown-uuid");
        when(jwtUtil.extractSessionClaims("token")).thenReturn(claims);
        when(userRepository.findBySessionUuid("unknown-uuid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.renewToken(new RenewTokenRequest("token")))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.MSG_TOKEN_INVALID);
    }

    @Test
    void renewToken_inactiveUser_throwsAuthException() {
        User inactive = User.builder()
                .username("admin").status(AppConstants.STATUS_INACTIVE).role(Role.ADMIN).build();

        Claims claims = mock(Claims.class);
        when(claims.get(AppConstants.CLAIM_UUID, String.class)).thenReturn("uuid-1");
        when(jwtUtil.extractSessionClaims("token")).thenReturn(claims);
        when(userRepository.findBySessionUuid("uuid-1")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> authService.renewToken(new RenewTokenRequest("token")))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.MSG_USER_INACTIVE);
    }

    @Test
    void renewToken_malformedToken_throwsAuthException() {
        when(jwtUtil.extractSessionClaims("garbage")).thenThrow(new RuntimeException("bad token"));

        assertThatThrownBy(() -> authService.renewToken(new RenewTokenRequest("garbage")))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.MSG_TOKEN_INVALID);
    }

    // ── isSessionTokenValid ──────────────────────────────────────────────────

    @Test
    void isSessionTokenValid_delegatesToJwtUtil() {
        when(jwtUtil.isSessionTokenValid("token")).thenReturn(true);
        assertThat(authService.isSessionTokenValid("token")).isTrue();

        when(jwtUtil.isSessionTokenValid("bad")).thenReturn(false);
        assertThat(authService.isSessionTokenValid("bad")).isFalse();
    }
}
