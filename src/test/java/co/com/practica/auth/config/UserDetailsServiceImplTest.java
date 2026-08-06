package co.com.practica.auth.config;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.rbac.ResolvedAuthorities;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.enums.Role;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.AuthorityResolutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository             userRepository;
    @Mock private AuthorityResolutionService authorityResolutionService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void stubAuthorities() {
        lenient().when(authorityResolutionService.resolve(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return ResolvedAuthorities.builder()
                    .primaryRole(u.getRole().name())
                    .roles(List.of(u.getRole().name()))
                    .permissions(List.of())
                    .groups(List.of())
                    .build();
        });
    }

    @Test
    void loadUserByUsername_activeUser_returnsEnabledUserDetails() {
        User user = User.builder()
                .username("admin")
                .passwordHash("$2a$10$hashed")
                .role(Role.ADMIN)
                .status(AppConstants.STATUS_ACTIVE)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsername_inactiveUser_returnsDisabledUserDetails() {
        User user = User.builder()
                .username("reader")
                .passwordHash("$2a$10$hashed")
                .role(Role.READONLY)
                .status(AppConstants.STATUS_INACTIVE)
                .build();
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("reader");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void loadUserByUsername_roleIsPrefixedCorrectly() {
        User user = User.builder()
                .username("user")
                .passwordHash("hash")
                .role(Role.USER)
                .status(AppConstants.STATUS_ACTIVE)
                .build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("user");

        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }
}
