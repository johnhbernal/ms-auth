package co.com.practica.auth.service.impl;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.rbac.ResolvedAuthorities;
import co.com.practica.auth.entity.AppRole;
import co.com.practica.auth.entity.DirectoryGroup;
import co.com.practica.auth.entity.Permission;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import co.com.practica.auth.repository.AppRoleRepository;
import co.com.practica.auth.repository.DirectoryGroupRepository;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.util.PracticaServiceClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorityResolutionServiceImplIntegrationTest {

    @Autowired AuthorityResolutionServiceImpl authorityResolutionService;
    @Autowired UserRepository                 userRepository;
    @Autowired DirectoryGroupRepository      directoryGroupRepository;
    @Autowired AppRoleRepository              appRoleRepository;
    @MockBean  PracticaServiceClient          practicaServiceClient;

    private User adminUser;

    @BeforeAll
    void loadAdmin() {
        adminUser = userRepository.findByUsernameWithGroups("admin").orElseThrow();
    }

    @Test
    void resolve_admin_includesPrimaryRoleGroupAndPermissions() {
        ResolvedAuthorities auth = authorityResolutionService.resolve(adminUser);

        assertThat(auth.getPrimaryRole()).isEqualTo("ADMIN");
        assertThat(auth.getRoles()).contains("ADMIN");
        assertThat(auth.getGroups()).contains("G-Admins");
        assertThat(auth.getPermissions()).contains("USER_ADMIN", "GROUP_ADMIN", "DIRECTORY_READ");
        assertThat(auth.getDistinguishedName()).contains("System Administrator");
    }

    @Test
    void seed_operatorRole_hasDirectoryReadPermission() {
        AppRole operator = appRoleRepository.findAllWithPermissions().stream()
                .filter(r -> "OPERATOR".equals(r.getName()))
                .findFirst()
                .orElseThrow();
        Set<String> codes = operator.getPermissions().stream().map(Permission::getCode)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(codes).contains("DIRECTORY_READ");
    }
}
