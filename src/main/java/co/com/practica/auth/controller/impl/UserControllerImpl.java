package co.com.practica.auth.controller.impl;

import co.com.practica.auth.controller.UserController;
import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.UserSummaryDto;
import co.com.practica.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserRepository userRepository;

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> listUsers() {
        log.info("GET /api/users — listing all users");
        List<UserSummaryDto> users = userRepository.findAll().stream()
                .map(u -> UserSummaryDto.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .status(u.getStatus())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .findFirst()
                .orElse("UNKNOWN");

        UserSummaryDto current = UserSummaryDto.builder()
                .username(auth.getName())
                .role(role)
                .build();

        log.debug("GET /api/users/me — caller: {}", auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(current));
    }
}
