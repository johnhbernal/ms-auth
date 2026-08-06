package co.com.practica.auth.dto.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,49}$", message = "Code must be UPPER_SNAKE_CASE")
    private String code;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$", message = "Module must be UPPER_SNAKE_CASE")
    private String module;
}
