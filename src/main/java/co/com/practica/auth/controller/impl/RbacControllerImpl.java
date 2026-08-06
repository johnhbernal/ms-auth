package co.com.practica.auth.controller.impl;

import co.com.practica.auth.controller.RbacController;
import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.rbac.CreateGroupRequest;
import co.com.practica.auth.dto.rbac.CreatePermissionRequest;
import co.com.practica.auth.dto.rbac.CreateRoleRequest;
import co.com.practica.auth.service.RbacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@Validated
@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RbacControllerImpl implements RbacController {

    private final RbacService rbacService;

    @Override
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DIRECTORY_READ')")
    public ResponseEntity<ApiResponse> listPermissions() {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.listPermissions()));
    }

    @Override
    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> createPermission(@RequestBody CreatePermissionRequest request) {
        log.info("POST /api/rbac/permissions — {}", request.getCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(rbacService.createPermission(request)));
    }

    @Override
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DIRECTORY_READ')")
    public ResponseEntity<ApiResponse> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.listRoles()));
    }

    @Override
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> createRole(@RequestBody CreateRoleRequest request) {
        log.info("POST /api/rbac/roles — {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(rbacService.createRole(request)));
    }

    @Override
    @GetMapping("/groups")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DIRECTORY_READ')")
    public ResponseEntity<ApiResponse> listGroups() {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.listGroups()));
    }

    @Override
    @PostMapping("/groups")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> createGroup(@RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(rbacService.createGroup(request)));
    }

    @Override
    @PostMapping("/groups/{groupId}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.addMember(groupId, userId)));
    }

    @Override
    @DeleteMapping("/groups/{groupId}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.removeMember(groupId, userId)));
    }

    @Override
    @PostMapping("/groups/{groupId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> assignRoleToGroup(@PathVariable Long groupId,
                                                         @PathVariable String roleName) {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.assignRoleToGroup(groupId, roleName)));
    }

    @Override
    @PostMapping("/roles/{roleName}/permissions/{permCode}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> assignPermissionToRole(@PathVariable String roleName,
                                                                @PathVariable String permCode) {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.assignPermissionToRole(roleName, permCode)));
    }

    @Override
    @DeleteMapping("/roles/{roleName}/permissions/{permCode}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_GROUP_ADMIN')")
    public ResponseEntity<ApiResponse> removePermissionFromRole(@PathVariable String roleName,
                                                                  @PathVariable String permCode) {
        return ResponseEntity.ok(ApiResponse.ok(rbacService.removePermissionFromRole(roleName, permCode)));
    }
}
