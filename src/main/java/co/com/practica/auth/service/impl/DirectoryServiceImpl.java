package co.com.practica.auth.service.impl;

import co.com.practica.auth.dto.rbac.DirectoryMeDto;
import co.com.practica.auth.dto.rbac.ResolvedAuthorities;
import co.com.practica.auth.entity.User;
import co.com.practica.auth.exception.ResourceNotFoundException;
import co.com.practica.auth.repository.UserRepository;
import co.com.practica.auth.service.AuthorityResolutionService;
import co.com.practica.auth.service.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final UserRepository             userRepository;
    private final AuthorityResolutionService authorityResolutionService;

    @Override
    @Transactional(readOnly = true)
    public DirectoryMeDto currentUserView(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        ResolvedAuthorities auth = authorityResolutionService.resolve(user);
        return DirectoryMeDto.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .distinguishedName(auth.getDistinguishedName())
                .primaryRole(auth.getPrimaryRole())
                .memberOf(auth.getGroupDns())
                .groups(auth.getGroups())
                .roles(auth.getRoles())
                .permissions(auth.getPermissions())
                .build();
    }
}
