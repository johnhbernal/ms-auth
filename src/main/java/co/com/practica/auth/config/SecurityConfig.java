package co.com.practica.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for ms-auth.
 *
 * <p>ms-auth is the authentication gateway: login and renew endpoints are PUBLIC
 * (no token required). All other endpoints require authentication.
 *
 * <p>{@code @EnableGlobalMethodSecurity} enables {@code @PreAuthorize} on controllers,
 * e.g. {@code @PreAuthorize("hasRole('ADMIN')")}.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/api/auth/renew",
        "/api/auth/validate",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/v3/api-docs",
        "/h2-console/**"
    };

    /**
     * Configures the security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled — not needed for stateless REST APIs.</li>
     *   <li>Session policy STATELESS — authentication is handled via JWT.</li>
     *   <li>Public endpoints are open; everything else requires authentication.</li>
     *   <li>Frame options set to SAMEORIGIN to allow H2 console in dev.</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            .and()
            .headers()
                .frameOptions().sameOrigin()
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder using BCrypt with strength 10.
     *
     * <p>BCrypt automatically generates a random salt per password and is
     * resistant to brute-force attacks. Strength 10 is the recommended
     * balance between security and performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
