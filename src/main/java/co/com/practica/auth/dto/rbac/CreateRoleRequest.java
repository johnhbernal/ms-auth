package co.com.practica.auth.dto.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$", message = "Role name must be UPPER_SNAKE_CASE")
    private String name;

    @Size(max = 255)
    private String description;

    /** Optional permission codes to attach on create. */
    @Builder.Default
    private List<String> permissionCodes = new ArrayList<>();
}
