package co.com.practica.auth.service;

import co.com.practica.auth.dto.AdminResetPasswordResponse;
import co.com.practica.auth.dto.ForgotPasswordRequest;
import co.com.practica.auth.dto.MessageResponse;
import co.com.practica.auth.dto.ResetPasswordRequest;

public interface PasswordResetService {

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    AdminResetPasswordResponse adminResetPassword(Long userId, String newPasswordOrNull);
}
