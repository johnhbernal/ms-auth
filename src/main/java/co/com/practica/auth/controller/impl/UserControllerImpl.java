package co.com.practica.auth.controller.impl;

import co.com.practica.auth.controller.UserController;
import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.RegisterRequest;
import co.com.practica.auth.dto.UserSummaryDto;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import javax.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserRepository userRepository;
    private final UserService    userService;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/users — registering new user");
        UserSummaryDto dto = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
    }

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> listUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        size = Math.min(size, 100); // cap at 100 per page
        log.info("GET /api/users — page={} size={}", page, size);
        List<UserSummaryDto> users = userRepository
                .findAll(PageRequest.of(page, size, Sort.by("id")))
                .stream()
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
