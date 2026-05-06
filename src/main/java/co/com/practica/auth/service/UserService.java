package co.com.practica.auth.service;

import co.com.practica.auth.dto.RegisterRequest;
import co.com.practica.auth.dto.UserSummaryDto;

public interface UserService {

    /**
     * Registers a new user account.
     *
     * @param request registration data (username, password, fullName, email, optional role)
     * @return summary of the created user (no password hash)
     * @throws co.com.practica.auth.exception.ConflictException if username or email is already taken
     */
    UserSummaryDto register(RegisterRequest request);
}
