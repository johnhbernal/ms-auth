package co.com.practica.auth.repository;

import co.com.practica.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for the {@link User} entity.
 * Spring Data auto-generates SQL from method names at startup.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds an active (or inactive) user by username and status.
     * Used during login to ensure only active accounts can authenticate.
     *
     * @param username account username
     * @param status   {@code "A"} for active, {@code "I"} for inactive
     * @return matching user wrapped in an {@link Optional}
     */
    Optional<User> findByUsernameAndStatus(String username, String status);

    /**
     * Finds a user by username regardless of status.
     *
     * @param username account username
     * @return matching user wrapped in an {@link Optional}
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their current session UUID.
     * Used during token renewal to locate the session owner.
     *
     * @param sessionUuid the UUID embedded in the session JWT
     * @return matching user wrapped in an {@link Optional}
     */
    Optional<User> findBySessionUuid(String sessionUuid);

    /**
     * Checks whether a username already exists in the database.
     *
     * @param username account username to check
     * @return {@code true} if the username is taken
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether an email address already exists in the database.
     *
     * @param email email address to check
     * @return {@code true} if the email is already registered
     */
    boolean existsByEmail(String email);
}
