package co.com.practica.auth.service.impl;

import co.com.practica.auth.dto.rbac.AppRoleDto;
import co.com.practica.auth.dto.rbac.CreateGroupRequest;
import co.com.practica.auth.dto.rbac.CreatePermissionRequest;
import co.com.practica.auth.dto.rbac.CreateRoleRequest;
import co.com.practica.auth.dto.rbac.DirectoryGroupDto;
import co.com.practica.auth.dto.rbac.PermissionDto;
import co.com.practica.auth.entity.AppRole;
import co.com.practica.auth.entity.DirectoryGroup;
import co.com.practica.auth.entity.Permission;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.exception.ConflictException;
import co.com.practica.auth.exception.ResourceNotFoundException;
import co.com.practica.auth.repository.AppRoleRepository;
import co.com.practica.auth.repository.DirectoryGroupRepository;
import co.com.practica.auth.repository.PermissionRepository;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final PermissionRepository     permissionRepository;
    private final AppRoleRepository        appRoleRepository;
    private final DirectoryGroupRepository directoryGroupRepository;
    private final UserRepository           userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppRoleDto> listRoles() {
        return appRoleRepository.findAllWithPermissions().stream()
                .sorted(Comparator.comparing(AppRole::getName))
                .map(this::toRoleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectoryGroupDto> listGroups() {
        return directoryGroupRepository.findAllWithRolesAndPermissions().stream()
                .sorted(Comparator.comparing(DirectoryGroup::getName))
                .map(this::toGroupDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PermissionDto createPermission(CreatePermissionRequest request) {
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        String module = request.getModule().trim().toUpperCase(Locale.ROOT);
        if (permissionRepository.existsByCode(code)) {
            throw new ConflictException("Permission already exists: " + code);
        }
        Permission saved = permissionRepository.save(Permission.builder()
                .code(code)
                .description(request.getDescription().trim())
                .module(module)
                .build());
        return toPermissionDto(saved);
    }

    @Override
    @Transactional
    public AppRoleDto createRole(CreateRoleRequest request) {
        String name = request.getName().trim().toUpperCase(Locale.ROOT);
        if (appRoleRepository.existsByName(name)) {
            throw new ConflictException("Role already exists: " + name);
        }
        Set<Permission> perms = new HashSet<>();
        if (request.getPermissionCodes() != null) {
            for (String code : request.getPermissionCodes()) {
                perms.add(permissionRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + code)));
            }
        }
        AppRole saved = appRoleRepository.save(AppRole.builder()
                .name(name)
                .description(request.getDescription())
                .permissions(perms)
                .build());
        return toRoleDto(saved);
    }

    @Override
    @Transactional
    public DirectoryGroupDto createGroup(CreateGroupRequest request) {
        if (directoryGroupRepository.existsByName(request.getName())) {
            throw new ConflictException("Group name already exists: " + request.getName());
        }
        DirectoryGroup group = DirectoryGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .distinguishedName(request.getDistinguishedName())
                .build();
        return toGroupDto(directoryGroupRepository.save(group));
    }

    @Override
    @Transactional
    public DirectoryGroupDto addMember(Long groupId, Long userId) {
        DirectoryGroup group = loadGroupWithDetails(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        group.getMembers().add(user);
        user.getDirectoryGroups().add(group);
        return toGroupDto(directoryGroupRepository.save(group));
    }

    @Override
    @Transactional
    public DirectoryGroupDto removeMember(Long groupId, Long userId) {
        DirectoryGroup group = loadGroupWithDetails(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        group.getMembers().remove(user);
        user.getDirectoryGroups().remove(group);
        return toGroupDto(directoryGroupRepository.save(group));
    }

    @Override
    @Transactional
    public DirectoryGroupDto assignRoleToGroup(Long groupId, String roleName) {
        DirectoryGroup group = loadGroupWithDetails(groupId);
        AppRole role = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        group.getAppRoles().add(role);
        return toGroupDto(directoryGroupRepository.save(group));
    }

    @Override
    @Transactional
    public AppRoleDto assignPermissionToRole(String roleName, String permCode) {
        AppRole role = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        Permission perm = permissionRepository.findByCode(permCode)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permCode));
        role.getPermissions().add(perm);
        return toRoleDto(appRoleRepository.save(role));
    }

    @Override
    @Transactional
    public AppRoleDto removePermissionFromRole(String roleName, String permCode) {
        AppRole role = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        Permission perm = permissionRepository.findByCode(permCode)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permCode));
        role.getPermissions().remove(perm);
        return toRoleDto(appRoleRepository.save(role));
    }

    private DirectoryGroup loadGroupWithDetails(Long groupId) {
        return directoryGroupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
    }

    private PermissionDto toPermissionDto(Permission p) {
        return PermissionDto.builder()
                .id(p.getId())
                .code(p.getCode())
                .description(p.getDescription())
                .module(p.getModule())
                .build();
    }

    private AppRoleDto toRoleDto(AppRole role) {
        List<String> perms = role.getPermissions().stream()
                .map(Permission::getCode)
                .sorted()
                .collect(Collectors.toList());
        return AppRoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(perms)
                .build();
    }

    private DirectoryGroupDto toGroupDto(DirectoryGroup group) {
        List<String> roles = group.getAppRoles().stream()
                .map(AppRole::getName)
                .sorted()
                .collect(Collectors.toList());
        List<String> members = group.getMembers().stream()
                .map(User::getUsername)
                .sorted()
                .collect(Collectors.toList());
        return DirectoryGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .distinguishedName(group.getDistinguishedName())
                .roles(roles)
                .members(members)
                .build();
    }
}
