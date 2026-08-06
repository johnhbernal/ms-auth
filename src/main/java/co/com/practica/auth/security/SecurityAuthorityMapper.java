package co.com.practica.auth.security;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.rbac.ResolvedAuthorities;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Maps JWT claims / resolved AuthZ to Spring Security authorities. */
public final class SecurityAuthorityMapper {

    private SecurityAuthorityMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Collection<? extends GrantedAuthority> fromClaims(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        addRoles(authorities, claims);
        addPermissions(authorities, claims);
        return authorities;
    }

    public static Collection<? extends GrantedAuthority> fromResolved(ResolvedAuthorities resolved) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (resolved.getRoles() != null) {
            resolved.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        if (resolved.getPermissions() != null) {
            resolved.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority("PERM_" + p)));
        }
        return authorities;
    }

    @SuppressWarnings("unchecked")
    private static void addRoles(List<SimpleGrantedAuthority> authorities, Claims claims) {
        List<String> roles = claims.get(AppConstants.CLAIM_ROLES, List.class);
        if (roles != null && !roles.isEmpty()) {
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
            return;
        }
        String role = claims.get(AppConstants.CLAIM_ROLE, String.class);
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
    }

    @SuppressWarnings("unchecked")
    private static void addPermissions(List<SimpleGrantedAuthority> authorities, Claims claims) {
        List<String> permissions = claims.get(AppConstants.CLAIM_PERMISSIONS, List.class);
        if (permissions == null) {
            return;
        }
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority("PERM_" + p)));
    }

    public static List<String> emptyIfNull(List<String> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
