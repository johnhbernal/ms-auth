package co.com.practica.auth.service;

import co.com.practica.auth.dto.rbac.ResolvedAuthorities;
import co.com.practica.auth.entity.User;

/**
 * Resolves effective roles and permissions from group membership (AuthZ).
 */
public interface AuthorityResolutionService {

    ResolvedAuthorities resolve(User user);

    String buildUserDistinguishedName(User user);
}
