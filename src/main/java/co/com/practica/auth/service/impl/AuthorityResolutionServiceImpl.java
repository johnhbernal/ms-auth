package co.com.practica.auth.service.impl;

import co.com.practica.auth.dto.rbac.ResolvedAuthorities;
import co.com.practica.auth.entity.AppRole;
import co.com.practica.auth.entity.DirectoryGroup;
import co.com.practica.auth.entity.Permission;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.repository.AppRoleRepository;
import co.com.practica.auth.repository.DirectoryGroupRepository;
import co.com.practica.auth.service.AuthorityResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * AuthZ resolution: User → DirectoryGroup (memberOf) → AppRole → Permission.
 *
 * <p>The legacy {@link User#getRole()} enum is always included as a role so
 * {@code hasRole('ADMIN')} and ms-practica Feign {@code role} claim stay compatible.
 */
@Service
@RequiredArgsConstructor
public class AuthorityResolutionServiceImpl implements AuthorityResolutionService {

    private static final String USER_DN_TEMPLATE = "CN=%s,OU=Users,DC=practica,DC=local";

    private final DirectoryGroupRepository directoryGroupRepository;
    private final AppRoleRepository      appRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public ResolvedAuthorities resolve(User user) {
        Set<String> roleNames = new LinkedHashSet<>();
        Set<String> permCodes = new LinkedHashSet<>();
        Set<String> groupNames = new LinkedHashSet<>();
        Set<String> groupDns = new LinkedHashSet<>();

        roleNames.add(user.getRole().name());

        List<DirectoryGroup> groups = directoryGroupRepository.findAllByMemberUserId(user.getId());
        for (DirectoryGroup group : groups) {
            groupNames.add(group.getName());
            groupDns.add(group.getDistinguishedName());
            for (AppRole appRole : group.getAppRoles()) {
                roleNames.add(appRole.getName());
                for (Permission permission : appRole.getPermissions()) {
                    permCodes.add(permission.getCode());
                }
            }
        }

        return ResolvedAuthorities.builder()
                .primaryRole(user.getRole().name())
                .roles(new ArrayList<>(roleNames))
                .permissions(new ArrayList<>(permCodes))
                .groups(new ArrayList<>(groupNames))
                .groupDns(new ArrayList<>(groupDns))
                .distinguishedName(buildUserDistinguishedName(user))
                .build();
    }

    @Override
    public String buildUserDistinguishedName(User user) {
        return String.format(USER_DN_TEMPLATE, user.getFullName());
    }
}
