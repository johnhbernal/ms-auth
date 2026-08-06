package co.com.practica.auth.service;

import co.com.practica.auth.dto.rbac.AppRoleDto;
import co.com.practica.auth.dto.rbac.CreateGroupRequest;
import co.com.practica.auth.dto.rbac.CreatePermissionRequest;
import co.com.practica.auth.dto.rbac.CreateRoleRequest;
import co.com.practica.auth.dto.rbac.DirectoryGroupDto;
import co.com.practica.auth.dto.rbac.PermissionDto;

import java.util.List;

public interface RbacService {

    List<PermissionDto> listPermissions();

    List<AppRoleDto> listRoles();

    List<DirectoryGroupDto> listGroups();

    PermissionDto createPermission(CreatePermissionRequest request);

    AppRoleDto createRole(CreateRoleRequest request);

    DirectoryGroupDto createGroup(CreateGroupRequest request);

    DirectoryGroupDto addMember(Long groupId, Long userId);

    DirectoryGroupDto removeMember(Long groupId, Long userId);

    DirectoryGroupDto assignRoleToGroup(Long groupId, String roleName);

    AppRoleDto assignPermissionToRole(String roleName, String permCode);

    AppRoleDto removePermissionFromRole(String roleName, String permCode);
}
