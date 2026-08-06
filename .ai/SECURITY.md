# SECURITY — ms-auth

> Dual-secret JWT · session UUID revocation · hashed tokens at rest · lockout · rate limit · OWASP notes

## Dual JWT secrets

| Property | Env | Use |
|----------|-----|-----|
| `app.jwt.secret-master` | `APP_JWT_SECRET_MASTER` | Sign/verify master tokens (24h) |
| `app.jwt.secret-session` | `APP_JWT_SECRET_SESSION` | Sign/verify session tokens (15m) |

- Keys must be ≥ 32 bytes for HMAC-SHA256. `JwtUtil.init()` throws `IllegalStateException` if null/blank or shorter.
- No defaults in base `application.properties`. Dev/test profiles supply local secrets only.
- **Master token** hashed SHA-256 (Base64) before DB / Feign persistence.
- **Session token** likewise hashed before `USERS.SESSION_TOKEN` on login **and** renew. Client receives plaintext JWT once; DB never stores the raw session JWT.
- **Revocation** remains via `sessionUuid` (null on logout; rotated on renew).

## Session validity (defense in depth)

A session is valid only if:

1. JWT signature + expiry OK (`JwtUtil.isSessionTokenValid`)
2. Claim `uuid` present and `UserRepository.findBySessionUuid` hits

Used by: `JwtAuthFilter`, `AuthServiceImpl.isSessionTokenValid`. Renew rotates UUID → old JWT dies without blacklist.

## Actuator

- Exposed: `health` only (`management.endpoints.web.exposure.include=health`).
- `management.endpoint.health.show-details=never`.
- `SecurityConfig` permits `/actuator/health` and `/actuator/health/**`.

## Account lockout

- After `MAX_FAILED_ATTEMPTS` (5) consecutive failures → `lockedUntil` = now + 15 min.
- Reset attempts on successful login.
- Generic credential error message (no user enumeration).

## Rate limiting

- `RateLimitFilter`: `POST /api/auth/login` → 10 req/min/IP (Bucket4j in-memory).
- Returns JSON `429`. Note: multi-instance needs shared store later (not in scope).

## OWASP-oriented checklist

| Topic | Status in ms-auth |
|-------|-------------------|
| Broken access control | Role claims + `@PreAuthorize`; UUID DB check |
| Cryptographic failures | BCrypt(10); dual secrets; hashed tokens at rest; no default prod secrets |
| Injection | JPA params; validation on DTOs |
| Security misconfig | Swagger/H2 off by default; prod profile + Flyway validate; profile must be explicit |
| Auth failures | Lockout + rate limit + generic errors |
| Sensitive data | `Cache-Control: no-store` on login/renew; no password in responses |

## CORS

`APP_CORS_ALLOWED_ORIGINS` comma-separated. Credentials false. Tighten in prod.

## IDE inspections vs real defects

| IDE message | Reality |
|-------------|---------|
| `Cannot resolve symbol 'String'` | **JDK not configured** — run `scripts/setup-intellij-sdk.ps1` (IntelliJ) or rely on `.vscode/settings.json` (Cursor). Project SDK name: `temurin-17`. Not an OWASP gap. |
| `Method renewToken/validateToken is never used` | **False positive** when Project JDK is missing (Spring model not loaded). Fix SDK first; mappings on `AuthController` are MVC entry points. |
| `Typo: In word 'practica'` | Package/domain name — dictionary in `.idea/misc.xml` + Cursor `cSpell.words`. Not a defect. |
| `Typo: practica` | Domain name `co.com.practica` — add to spell dictionary (`.vscode/settings.json` `cSpell.words`). |

## Out of scope (do not fake)

- Active Directory / LDAP (see ACTIVE-DIRECTORY.md)
- Redis-backed rate limit / token blacklist
- Spring Boot 3 security rewrite
