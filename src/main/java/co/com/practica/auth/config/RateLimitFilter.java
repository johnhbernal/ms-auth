package co.com.practica.auth.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits POST /api/auth/login to 10 attempts per IP per minute.
 * Runs before the security filter chain to block brute-force attacks early.
 */
@Log4j2
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int      LOGIN_CAPACITY  = 10;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("/api/auth/login".equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = getClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(ip, k ->
                Bucket.builder()
                    .addLimit(Bandwidth.classic(LOGIN_CAPACITY,
                        Refill.intervally(LOGIN_CAPACITY, REFILL_DURATION)))
                    .build());

            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded for IP: {}", ip);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                    "{\"code\":\"429\",\"description\":\"Too many login attempts. Try again in a minute.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
