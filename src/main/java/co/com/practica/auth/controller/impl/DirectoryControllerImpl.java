package co.com.practica.auth.controller.impl;

import co.com.practica.auth.controller.DirectoryController;
import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.service.DirectoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryControllerImpl implements DirectoryController {

    private final DirectoryService directoryService;

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("GET /api/directory/me — caller: {}", username);
        return ResponseEntity.ok(ApiResponse.ok(directoryService.currentUserView(username)));
    }
}
