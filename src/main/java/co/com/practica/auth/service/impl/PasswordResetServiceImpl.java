package co.com.practica.auth.service.impl;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.AdminResetPasswordResponse;
import co.com.practica.auth.dto.ForgotPasswordRequest;
import co.com.practica.auth.dto.MessageResponse;
import co.com.practica.auth.dto.ResetPasswordRequest;
import co.com.practica.auth.entity.PasswordResetToken;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.exception.BadRequestException;
import co.com.practica.auth.exception.ResourceNotFoundException;
import co.com.practica.auth.repository.PasswordResetTokenRepository;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Portfolio-friendly password reset: one-time tokens hashed at rest.
 * In dev/stack the raw token is logged (never emailed as plaintext password).
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository               userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder              passwordEncoder;
    private final Environment                  environment;

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailAndStatus(request.getEmail(), AppConstants.STATUS_ACTIVE)
                .ifPresent(this::issueResetToken);
        return MessageResponse.builder()
                .message(AppConstants.MSG_FORGOT_PASSWORD_SENT)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken record = tokenRepository
                .findByTokenHashAndUsedAtIsNull(hashToken(request.getToken()))
                .orElseThrow(() -> new BadRequestException(AppConstants.MSG_RESET_TOKEN_INVALID));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(AppConstants.MSG_RESET_TOKEN_INVALID);
        }

        User user = record.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        invalidateUserSessions(user);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        record.setUsedAt(LocalDateTime.now());
        tokenRepository.save(record);

        log.info("Password reset completed for user: {}", user.getUsername());
        return MessageResponse.builder()
                .message(AppConstants.MSG_PASSWORD_RESET_SUCCESS)
                .build();
    }

    @Override
    @Transactional
    public AdminResetPasswordResponse adminResetPassword(Long userId, String newPasswordOrNull) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(newPasswordOrNull));
            invalidateUserSessions(user);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
            log.info("Admin set new password for user: {}", user.getUsername());
            return AdminResetPasswordResponse.builder()
                    .message(AppConstants.MSG_ADMIN_PASSWORD_SET)
                    .build();
        }

        String rawToken = issueResetToken(user);
        AdminResetPasswordResponse.AdminResetPasswordResponseBuilder builder =
                AdminResetPasswordResponse.builder()
                        .message(AppConstants.MSG_ADMIN_RESET_TOKEN_ISSUED);
        if (exposeTokenInResponse()) {
            builder.resetToken(rawToken);
        }
        return builder.build();
    }

    private String issueResetToken(User user) {
        tokenRepository.invalidateActiveTokensForUser(user.getId(), LocalDateTime.now());

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusMinutes(AppConstants.PASSWORD_RESET_EXPIRATION_MINS))
                .build();
        tokenRepository.save(token);

        if (exposeTokenInResponse()) {
            log.info("Password reset token issued for {} — dev/stack demo URL: "
                            + "/reset-password?token={} (expires in {} min)",
                    user.getEmail(), rawToken, AppConstants.PASSWORD_RESET_EXPIRATION_MINS);
        } else {
            log.info("Password reset token issued for user id {} (token not logged in prod)", user.getId());
        }
        return rawToken;
    }

    private void invalidateUserSessions(User user) {
        user.setSessionToken(null);
        user.setSessionUuid(null);
        user.setSessionTokenExpiresAt(null);
    }

    private boolean exposeTokenInResponse() {
        return environment.acceptsProfiles(Profiles.of("dev", "stack"));
    }

    private static String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
