package co.com.practica.auth.dto.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Response for {@code GET /api/directory/me} — AuthN + AuthZ demo. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryMeDto {

    private String username;
    private String fullName;
    private String email;
    private String distinguishedName;
    private String primaryRole;
    private List<String> memberOf;
    private List<String> groups;
    private List<String> roles;
    private List<String> permissions;
}
