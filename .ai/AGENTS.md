# ms-auth — AI Orchestrator

> Auth microservice for the Practica system. Stateless JWT gateway. **No inventar MRR / features fuera de alcance.**

## Identity

| | |
|---|---|
| Artifact | `co.com.practica:ms-auth:1.0.0` |
| Runtime | Java 17 · Spring Boot **2.7.18** (no major upgrade in hardening passes) |
| Port | `8081` |
| DB | H2 in-memory (dev/test) |
| Downstream | `ms-practica` via OpenFeign (master token param store; login must not fail if unreachable) |

## Specialist index

| File | Use when |
|------|----------|
| [JAVA.md](JAVA.md) | Language, package layout, Lombok, tests |
| [SPRINGBOOT.md](SPRINGBOOT.md) | Boot 2.7 patterns, Security, profiles, Feign |
| [SECURITY.md](SECURITY.md) | Dual JWT secrets, lockout, rate limit, OWASP |
| [DATABASE.md](DATABASE.md) | User entity, session UUID revocation model |
| [ACTIVE-DIRECTORY.md](ACTIVE-DIRECTORY.md) | AD sim + module RBAC (VENDEDOR/INVENTARIO) |
| [QA.md](QA.md) | JUnit gates · Kilele: API ≠ visual |
| [CI.md](CI.md) | GitHub Actions gates |

**Council stack:** `practica-stack/.ai/AGENTS.md`

## Scope rules

- Do what was asked; nothing more.
- Prefer edit over create. Keep files under 500 lines.
- Do **not** upgrade Spring Boot major unless explicitly requested.
- Secrets only via env / `application-dev.properties` (dev only). Never commit prod secrets.
- Validate at API boundaries (`@Valid`, `@Validated` + constraint handlers).

## Token model (canonical)

1. **Login** → BCrypt check → master JWT (24h, hashed SHA-256 in DB + Feign to ms-practica) → session JWT (15m) with `uuid` claim persisted as `USERS.SESSION_UUID`.
2. **Request auth** → `JwtAuthFilter`: signature OK **and** `SESSION_UUID` still in DB.
3. **Renew** → rotate UUID (old session invalid immediately).
4. **Logout** → null `sessionToken` / `sessionUuid`.

## Public endpoints

`POST /api/auth/login|renew|logout` · `GET /api/auth/validate` · Swagger/H2 only when enabled.

Protected: everything else (incl. `POST /api/users` ADMIN register, `GET /api/users`).

## Agent routing

| Task | Read first |
|------|------------|
| Auth/JWT bug | SECURITY.md → AuthServiceImpl · JwtAuthFilter · JwtUtil |
| CI red | CI.md → pom.xml · failing test |
| Persistence | DATABASE.md |
| AD/LDAP proposal | ACTIVE-DIRECTORY.md (design only until approved) |
