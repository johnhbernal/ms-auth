package co.com.practica.auth.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Admin password reset. Provide {@code newPassword} to set directly, or omit it to issue a one-time reset token.
 */
@Data
public class AdminResetPasswordRequest {

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
        message = "Password must contain at least one uppercase letter, one digit, and one special character (@$!%*?&)"
    )
    private String newPassword;
}
