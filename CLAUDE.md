# Project Claude Configuration

Extends global CLAUDE.md. Project-specific rules take precedence over global ones.

## Project Identity

- **name:** ms-auth
- **stack:** Java 17 · Spring Boot 2.7.18 · Spring Security · JPA/H2 · JJWT 0.11.5 · Bucket4j 7.6 · OpenFeign · springdoc 1.7
- **role:** Authentication microservice (session JWT + dual secrets + lockout/rate-limit)
- **port:** 8081
- **language:** es/en OK; code/comments in English as existing
- **orchestrator:** [`.ai/AGENTS.md`](.ai/AGENTS.md)

## Architecture Notes

- Stateless REST auth gateway for Practica.
- Session validity = JWT signature **and** `USERS.SESSION_UUID` match (logout/renew revoke without blacklist).
- Master token (24h) hashed at rest; session token (15m) to clients.
- Feign to ms-practica is best-effort on login (must not block auth).
- Specialist docs: `.ai/JAVA.md`, `.ai/SPRINGBOOT.md`, `.ai/SECURITY.md`, `.ai/DATABASE.md`, `.ai/ACTIVE-DIRECTORY.md`, `.ai/CI.md`.

## Conventions

- Prefer editing existing files; keep changes focused.
- Do **not** upgrade Spring Boot major unless explicitly requested.
- Do **not** invent MRR or unrelated product features.
- Profiles: never hardcode `spring.profiles.active=dev` in base props — use `-Dspring-boot.run.profiles=dev`.
- Validate at boundaries; map `ConstraintViolationException` → 400.
- Register users: `POST /api/users` (ADMIN), not `/api/users/register`.

## Out of Scope

- Active Directory / LDAP implementation (design only in `.ai/ACTIVE-DIRECTORY.md`)
- Spring Boot 3 / Jakarta migration
- Redis-backed rate limiting / distributed session store
- Frontend work (see ms-frontend)

## Active Stack Profiles

- java
- spring-boot-2.7
- maven
- h2-jpa
- jwt-security
