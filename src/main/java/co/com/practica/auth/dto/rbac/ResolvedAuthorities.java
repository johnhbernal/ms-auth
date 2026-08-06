package co.com.practica.auth.dto.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Effective AuthZ view for a user: groups → roles → permissions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedAuthorities {

    /** Legacy primary role from {@code User.role} (ms-practica Feign compat). */
    private String primaryRole;

    private List<String> roles;
    private List<String> permissions;
    private List<String> groups;
    private List<String> groupDns;
    private String distinguishedName;
}
