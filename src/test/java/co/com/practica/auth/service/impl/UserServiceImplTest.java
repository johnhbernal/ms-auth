package co.com.practica.auth.service.impl;

import co.com.practica.auth.dto.RegisterRequest;
import co.com.practica.auth.dto.UserSummaryDto;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import co.com.practica.auth.exception.ConflictException;
import co.com.practica.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository  userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository  = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(4); // low cost for tests
        userService     = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void register_validRequest_returnsUserSummaryDto() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        RegisterRequest request = RegisterRequest.builder()
                .username("alice").password("Secret1!").fullName("Alice Smith")
                .email("alice@test.com").role("USER").build();

        UserSummaryDto result = userService.register(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getStatus()).isEqualTo("A");
    }

    @Test
    void register_omittedRole_defaultsToUser() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = RegisterRequest.builder()
                .username("bob").password("Secret1!").fullName("Bob")
                .email("bob@test.com").role(null).build();

        UserSummaryDto result = userService.register(request);

        assertThat(result.getRole()).isEqualTo(Role.USER.name());
    }

    @Test
    void register_duplicateUsername_throwsConflictException() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .username("admin").password("Secret1!").fullName("X")
                .email("x@test.com").role(null).build();

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsConflictException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").password("Secret1!").fullName("X")
                .email("taken@test.com").role(null).build();

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }
}
