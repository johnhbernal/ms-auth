package co.com.practica.auth.config;

import co.com.practica.auth.entity.AppRole;
import co.com.practica.auth.entity.DirectoryGroup;
import co.com.practica.auth.entity.Permission;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import co.com.practica.auth.repository.AppRoleRepository;
import co.com.practica.auth.repository.DirectoryGroupRepository;
import co.com.practica.auth.repository.PermissionRepository;
import co.com.practica.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads seed users and RBAC data in {@code dev} or {@code stack} profiles only.
 * <p>Module demo: AppRole {@code VENDEDOR} → {@code INVENTARIO_PRECIO_READ} only
 * (no price/stock write). Login as {@code seller} / {@code Seller123!}.
 */
@Log4j2
@Component
@Profile({"dev", "stack"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DN_GROUPS = "OU=Groups,DC=practica,DC=local";

    private final UserRepository           userRepository;
    private final PasswordEncoder          passwordEncoder;
    private final PermissionRepository     permissionRepository;
    private final AppRoleRepository        appRoleRepository;
    private final DirectoryGroupRepository directoryGroupRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Loading DEV seed data ===");
        seedPermissions();
        seedAppRoles();
        syncAdminPermissions();
        syncVendedorPermissions();
        seedDirectoryGroups();
        createUserIfAbsent("admin",  "Admin123!", "System Administrator", "admin@practica.com",  Role.ADMIN,   "G-Admins");
        createUserIfAbsent("user",   "User123!",  "Regular User",         "user@practica.com",   Role.USER,    "G-Operators");
        createUserIfAbsent("reader", "Read123!",  "Read-Only User",       "reader@practica.com", Role.READONLY, "G-Readers");
        createUserIfAbsent("seller", "Seller123!", "Sales Seller",        "seller@practica.com", Role.USER,    "G-Vendors");
        log.info("=== DEV seed data loaded successfully ===");
    }

    private void seedPermissions() {
        createPermissionIfAbsent("PARAMETRO_READ",  "Read practica parameters", "PARAMETROS");
        createPermissionIfAbsent("PARAMETRO_WRITE", "Write practica parameters", "PARAMETROS");
        createPermissionIfAbsent("USER_ADMIN",      "Manage user accounts", "RBAC");
        createPermissionIfAbsent("GROUP_ADMIN",     "Manage directory groups and memberships", "RBAC");
        createPermissionIfAbsent("DIRECTORY_READ",  "Read RBAC / directory metadata", "RBAC");
        createPermissionIfAbsent("INVENTARIO_PRECIO_READ",  "View inventory product prices", "INVENTARIO");
        createPermissionIfAbsent("INVENTARIO_PRECIO_WRITE", "Change inventory product prices", "INVENTARIO");
        createPermissionIfAbsent("INVENTARIO_STOCK_WRITE",  "Change inventory stock quantities", "INVENTARIO");
    }

    private void seedAppRoles() {
        createAppRoleIfAbsent("ADMIN", "Full application access", allPermissions());
        createAppRoleIfAbsent("USER", "Standard operator",
                codes("PARAMETRO_READ", "PARAMETRO_WRITE"));
        createAppRoleIfAbsent("READONLY", "Read-only access", codes("PARAMETRO_READ"));
        createAppRoleIfAbsent("OPERATOR", "Operator with directory read",
                codes("PARAMETRO_READ", "PARAMETRO_WRITE", "DIRECTORY_READ"));
        createAppRoleIfAbsent("VENDEDOR", "Seller — inventory prices read-only",
                codes("INVENTARIO_PRECIO_READ"));
    }

    /** Idempotent: ADMIN always receives newly seeded permissions (e.g. INVENTARIO_*). */
    private void syncAdminPermissions() {
        AppRole admin = appRoleRepository.findByName("ADMIN").orElse(null);
        if (admin == null) {
            return;
        }
        Set<String> existing = admin.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        boolean changed = false;
        for (Permission p : permissionRepository.findAll()) {
            if (!existing.contains(p.getCode())) {
                admin.getPermissions().add(p);
                changed = true;
            }
        }
        if (changed) {
            appRoleRepository.save(admin);
            log.info("Synced ADMIN role with latest permissions");
        }
    }

    /**
     * Idempotent: VENDEDOR stays price-read-only even if an older DB had extra grants
     * ({@code createAppRoleIfAbsent} alone would leave stale role_permissions).
     */
    private void syncVendedorPermissions() {
        AppRole vendedor = appRoleRepository.findByName("VENDEDOR").orElse(null);
        if (vendedor == null) {
            return;
        }
        Set<Permission> expected = codes("INVENTARIO_PRECIO_READ");
        Set<String> expectedCodes = expected.stream().map(Permission::getCode).collect(Collectors.toSet());
        Set<String> currentCodes = vendedor.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        if (currentCodes.equals(expectedCodes)) {
            return;
        }
        vendedor.getPermissions().clear();
        vendedor.getPermissions().addAll(expected);
        appRoleRepository.save(vendedor);
        log.info("Synced VENDEDOR role to INVENTARIO_PRECIO_READ only");
    }

    private void seedDirectoryGroups() {
        createGroupIfAbsent("G-Admins", "Administrators",
                "CN=G-Admins," + DN_GROUPS, "ADMIN");
        createGroupIfAbsent("G-Operators", "Operators",
                "CN=G-Operators," + DN_GROUPS, "OPERATOR");
        createGroupIfAbsent("G-Readers", "Readers",
                "CN=G-Readers," + DN_GROUPS, "READONLY");
        createGroupIfAbsent("G-Vendors", "Sales vendors",
                "CN=G-Vendors," + DN_GROUPS, "VENDEDOR");
    }

    private void createPermissionIfAbsent(String code, String description, String module) {
        if (!permissionRepository.existsByCode(code)) {
            permissionRepository.save(Permission.builder()
                    .code(code)
                    .description(description)
                    .module(module)
                    .build());
            log.info("Seed permission: {} [{}]", code, module);
        }
    }

    private void createAppRoleIfAbsent(String name, String description, Set<Permission> permissions) {
        if (appRoleRepository.existsByName(name)) {
            return;
        }
        AppRole role = AppRole.builder()
                .name(name)
                .description(description)
                .permissions(permissions)
                .build();
        appRoleRepository.save(role);
        log.info("Seed AppRole: {} ({} permissions)", name, permissions.size());
    }

    private void createGroupIfAbsent(String name, String description, String dn, String roleName) {
        if (directoryGroupRepository.existsByName(name)) {
            return;
        }
        AppRole role = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Missing seed role: " + roleName));
        DirectoryGroup group = DirectoryGroup.builder()
                .name(name)
                .description(description)
                .distinguishedName(dn)
                .appRoles(new LinkedHashSet<>(Set.of(role)))
                .build();
        directoryGroupRepository.save(group);
        log.info("Seed group: {} → {}", name, roleName);
    }

    private void createUserIfAbsent(String username, String rawPassword, String fullName,
                                    String email, Role role, String groupName) {
        User user = userRepository.findByUsername(username).orElse(null);
        DirectoryGroup group = directoryGroupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalStateException("Missing seed group: " + groupName));

        if (user == null) {
            user = userRepository.save(User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .email(email)
                    .role(role)
                    .status("A")
                    .build());
            log.info("Seed user created: {} ({})", username, role);
        }

        if (!user.getDirectoryGroups().contains(group)) {
            user.getDirectoryGroups().add(group);
            group.getMembers().add(user);
            userRepository.save(user);
            log.info("Seed membership: {} → {}", username, groupName);
        }
    }

    private Set<Permission> allPermissions() {
        return new LinkedHashSet<>(permissionRepository.findAll());
    }

    private Set<Permission> codes(String... codes) {
        Set<Permission> set = new LinkedHashSet<>();
        for (String code : codes) {
            set.add(permissionRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalStateException("Missing permission: " + code)));
        }
        return set;
    }
}
