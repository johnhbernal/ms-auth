package co.com.practica.auth.dto.rbac;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CreateGroupRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank
    @Size(max = 255)
    private String distinguishedName;
}
