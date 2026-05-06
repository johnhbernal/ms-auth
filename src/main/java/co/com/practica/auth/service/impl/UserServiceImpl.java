package co.com.practica.auth.service.impl;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.RegisterRequest;
import co.com.practica.auth.dto.UserSummaryDto;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import co.com.practica.auth.exception.ConflictException;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserSummaryDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException(AppConstants.MSG_USERNAME_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(AppConstants.MSG_EMAIL_EXISTS);
        }

        Role role = (request.getRole() == null || request.getRole().isBlank())
                ? Role.USER
                : Role.valueOf(request.getRole());

        User saved = userRepository.save(User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(role)
                .status(AppConstants.STATUS_ACTIVE)
                .build());

        log.info("User registered: {} ({})", saved.getUsername(), saved.getRole());

        return UserSummaryDto.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .status(saved.getStatus())
                .build();
    }
}
