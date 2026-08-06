package co.com.practica.auth.service.impl;

import co.com.practica.auth.constants.AppConstants;
import co.com.practica.auth.dto.directory.DirectoryBindResult;
import co.com.practica.auth.dto.rbac.ResolvedAuthorities;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.exception.AuthException;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.AuthorityResolutionService;
import co.com.practica.auth.service.SimulatedDirectoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Portfolio simulation of an LDAP bind against Active Directory.
 *
 * <p>Real AD would use {@code LdapContext} / StartTLS; here we reuse local
 * {@link User} records and BCrypt hashes, then expose DN + {@code memberOf}
 * from {@link co.com.practica.auth.entity.DirectoryGroup} membership.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class SimulatedDirectoryServiceImpl implements SimulatedDirectoryService {

    private final UserRepository             userRepository;
    private final PasswordEncoder            passwordEncoder;
    private final AuthorityResolutionService authorityResolutionService;

    @Override
    @Transactional(readOnly = true)
    public DirectoryBindResult bind(String username, String password) {
        User user = userRepository
                .findByUsernameAndStatus(username, AppConstants.STATUS_ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Directory bind failed — unknown user");
                    return new AuthException(AppConstants.MSG_INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Directory bind failed — invalid password");
            throw new AuthException(AppConstants.MSG_INVALID_CREDENTIALS);
        }

        ResolvedAuthorities auth = authorityResolutionService.resolve(user);
        log.info("Directory bind OK — user DN: {}", auth.getDistinguishedName());

        return DirectoryBindResult.builder()
                .user(user)
                .distinguishedName(auth.getDistinguishedName())
                .memberOf(auth.getGroupDns())
                .build();
    }
}
