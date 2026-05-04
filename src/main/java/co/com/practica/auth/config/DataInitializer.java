package co.com.practica.auth.config;

import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import co.com.practica.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads seed data in DEV profile only.
 * Creates three test users, one per available role.
 *
 * <pre>
 * ┌──────────────┬──────────────┬──────────┐
 * │ Username     │ Password     │ Role     │
 * ├──────────────┼──────────────┼──────────┤
 * │ admin        │ Admin123!    │ ADMIN    │
 * │ user         │ User123!     │ USER     │
 * │ reader       │ Read123!     │ READONLY │
 * └──────────────┴──────────────┴──────────┘
 * </pre>
 *
 * <p><b>Security note:</b> seed credentials are intentionally logged only at
 * INFO level in DEV profile. They must NEVER appear in production logs.
 */
@Log4j2
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Loading DEV seed data ===");
        createUserIfAbsent("admin",  "Admin123!", "System Administrator", "admin@practica.com",  Role.ADMIN);
        createUserIfAbsent("user",   "User123!",  "Regular User",         "user@practica.com",   Role.USER);
        createUserIfAbsent("reader", "Read123!",  "Read-Only User",       "reader@practica.com", Role.READONLY);
        log.info("=== DEV seed data loaded successfully ===");
    }

    /**
     * Creates a user only if the username does not already exist,
     * preventing duplicate inserts on application restart.
     */
    private void createUserIfAbsent(String username, String rawPassword,
                                     String fullName, String email, Role role) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .email(email)
                    .role(role)
                    .status("A")
                    .build());
            log.info("Seed user created: {} ({})", username, role);
        } else {
            log.debug("Seed user already exists, skipping: {}", username);
        }
    }
}
