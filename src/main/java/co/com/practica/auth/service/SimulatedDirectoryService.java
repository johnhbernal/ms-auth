package co.com.practica.auth.service;

import co.com.practica.auth.dto.directory.DirectoryBindResult;

/**
 * Simulated Active Directory bind — no real LDAP.
 * Validates credentials (BCrypt) and returns AD-like DN / memberOf for learning.
 */
public interface SimulatedDirectoryService {

    DirectoryBindResult bind(String username, String password);
}
