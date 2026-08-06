package co.com.practica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResetPasswordResponse {

    private String message;
    /** Plaintext reset token — returned only in dev/stack when no newPassword was supplied. */
    private String resetToken;
}
