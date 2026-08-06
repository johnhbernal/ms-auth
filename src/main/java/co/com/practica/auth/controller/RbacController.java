package co.com.practica.auth.controller;

import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.rbac.CreateGroupRequest;
import co.com.practica.auth.dto.rbac.CreatePermissionRequest;
import co.com.practica.auth.dto.rbac.CreateRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;

@Tag(name = "RBAC", description = "Module permissions, roles, AD-sim groups")
public interface RbacController {

    @Operation(summary = "List permissions (optionally by module in UI)")
    ResponseEntity<ApiResponse> listPermissions();

    @Operation(summary = "Create permission for a module")
    ResponseEntity<ApiResponse> createPermission(@Valid CreatePermissionRequest request);

    @Operation(summary = "List application roles with permissions")
    ResponseEntity<ApiResponse> listRoles();

    @Operation(summary = "Create application role")
    ResponseEntity<ApiResponse> createRole(@Valid CreateRoleRequest request);

    @Operation(summary = "List directory groups with roles and members")
    ResponseEntity<ApiResponse> listGroups();

    @Operation(summary = "Create directory group")
    ResponseEntity<ApiResponse> createGroup(@Valid CreateGroupRequest request);

    @Operation(summary = "Add user to group")
    ResponseEntity<ApiResponse> addMember(Long groupId, Long userId);

    @Operation(summary = "Remove user from group")
    ResponseEntity<ApiResponse> removeMember(Long groupId, Long userId);

    @Operation(summary = "Assign application role to group")
    ResponseEntity<ApiResponse> assignRoleToGroup(Long groupId, String roleName);

    @Operation(summary = "Assign permission to application role")
    ResponseEntity<ApiResponse> assignPermissionToRole(String roleName, String permCode);

    @Operation(summary = "Remove permission from application role")
    ResponseEntity<ApiResponse> removePermissionFromRole(String roleName, String permCode);
}
