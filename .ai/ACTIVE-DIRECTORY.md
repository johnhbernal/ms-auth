# ACTIVE-DIRECTORY — future LDAP (not implemented)

> Guidance only. ms-auth today is **local User + BCrypt**. Do not add AD code unless product asks.

## Goal

Optional enterprise login against Active Directory / LDAP while keeping session JWT issuance in ms-auth.

## Recommended wiring (Spring Boot 2.7)

1. Dependency: `spring-boot-starter-data-ldap` (and/or `spring-security-ldap`).
2. Properties (env-driven):
   - `spring.ldap.urls`
   - `spring.ldap.base`
   - `spring.ldap.username` / `password` (bind DN) **or** manager-less pattern
3. `AuthenticationManager` with `LdapAuthenticationProvider` **or** custom `AuthenticationProvider` that:
   - Binds user DN (`uid={0},ou=people` / `sAMAccountName` pattern for AD)
   - On success, loads/creates local `User` for roles + session UUID store
4. Keep **JWT dual-secret + session UUID** as the API auth mechanism — LDAP authenticates login only; do not put LDAP password on every request.

## AD-specific notes

- Prefer LDAPS / StartTLS; never plain LDAP in prod.
- Map groups → `Role` carefully (ADMIN rare).
- Lockout: coordinate with AD lockout policy; avoid double-punishing with local counter unless product requires.
- Feign master-token store remains local-user keyed.

## Migration sketch

| Phase | Work |
|-------|------|
| 1 | Feature flag `app.auth.mode=LOCAL\|LDAP` |
| 2 | LDAP provider behind flag; LOCAL default |
| 3 | Provisioning: sync email/fullName on first login |
| 4 | IT with embedded LDAP or Testcontainers |

## Explicit non-goals now

- No AD dependency in `pom.xml` until approved.
- No inventing corporate DN schemas without customer input.
