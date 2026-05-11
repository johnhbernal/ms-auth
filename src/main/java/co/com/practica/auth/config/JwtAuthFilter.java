package co.com.practica.auth.config;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil          jwtUtil;
    private final UserRepository   userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(AppConstants.AUTHORIZATION_HEADER);

        if (header == null || !header.startsWith(AppConstants.BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(AppConstants.BEARER_PREFIX.length());

        if (jwtUtil.isSessionTokenValid(token)) {
            Claims claims = jwtUtil.extractSessionClaims(token);
            String uuid = claims.get(AppConstants.CLAIM_UUID, String.class);

            // Verify the session UUID still exists in DB — catches revoked sessions
            if (uuid == null || !userRepository.findBySessionUuid(uuid).isPresent()) {
                log.warn("Session UUID not found — token revoked or invalid");
                chain.doFilter(request, response);
                return;
            }

            String username = claims.getSubject();
            String role     = claims.get(AppConstants.CLAIM_ROLE, String.class);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("JWT authenticated — role: {}", role);
        } else {
            log.warn("Invalid or expired JWT on {}", request.getRequestURI());
        }

        chain.doFilter(request, response);
    }
}
