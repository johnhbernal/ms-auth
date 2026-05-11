package co.com.practica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Request body for the user registration endpoint.
 *
 * <p>{@code role} is optional — omitting it defaults to {@code USER} in the service layer.
 * Valid values: {@code ADMIN}, {@code USER}, {@code READONLY}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
        message = "Password must contain at least one uppercase letter, one digit, and one special character (@$!%*?&)"
    )
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    @Pattern(
        regexp = "^(ADMIN|USER|READONLY)?$",
        message = "Role must be one of: ADMIN, USER, READONLY"
    )
    private String role;
}
