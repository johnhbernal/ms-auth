package co.com.practica.auth.config;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.security.SecurityAuthorityMapper;
import co.com.practica.auth.service.AuthorityResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository             userRepository;
    private final AuthorityResolutionService authorityResolutionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        boolean active = AppConstants.STATUS_ACTIVE.equals(user.getStatus());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                active,
                true,
                true,
                true,
                SecurityAuthorityMapper.fromResolved(authorityResolutionService.resolve(user))
        );
    }
}
