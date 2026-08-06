package co.com.practica.auth.dto.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryGroupDto {

    private Long id;
    private String name;
    private String description;
    private String distinguishedName;
    private List<String> roles;
    private List<String> members;
}
