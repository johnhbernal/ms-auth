package co.com.practica.auth.config;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_passesThrough_contextEmpty() throws Exception {
        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void headerWithoutBearerPrefix_passesThrough_contextEmpty() throws Exception {
        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();
        request.addHeader(AppConstants.AUTHORIZATION_HEADER, "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void validToken_setsSecurityContext() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get(AppConstants.CLAIM_ROLE, String.class)).thenReturn("ADMIN");

        when(jwtUtil.isSessionTokenValid("valid-token")).thenReturn(true);
        when(jwtUtil.extractSessionClaims("valid-token")).thenReturn(claims);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();
        request.addHeader(AppConstants.AUTHORIZATION_HEADER, "Bearer valid-token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("admin");
        assertThat(auth.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void invalidToken_contextEmpty_chainContinues() throws Exception {
        when(jwtUtil.isSessionTokenValid("bad-token")).thenReturn(false);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();
        request.addHeader(AppConstants.AUTHORIZATION_HEADER, "Bearer bad-token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verify(jwtUtil, never()).extractSessionClaims(any());
    }

    @Test
    void validToken_differentRole_authorityMappedCorrectly() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("reader");
        when(claims.get(AppConstants.CLAIM_ROLE, String.class)).thenReturn("READONLY");

        when(jwtUtil.isSessionTokenValid("readonly-token")).thenReturn(true);
        when(jwtUtil.extractSessionClaims("readonly-token")).thenReturn(claims);

        MockHttpServletRequest  request  = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain         chain    = new MockFilterChain();
        request.addHeader(AppConstants.AUTHORIZATION_HEADER, "Bearer readonly-token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo("reader");
        assertThat(auth.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_READONLY"));
    }
}
