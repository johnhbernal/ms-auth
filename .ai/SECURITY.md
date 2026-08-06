# SECURITY — ms-auth

> Dual-secret JWT · session UUID revocation · lockout · rate limit · OWASP notes

## Dual JWT secrets

| Property | Env | Use |
|----------|-----|-----|
| `app.jwt.secret-master` | `APP_JWT_SECRET_MASTER` | Sign/verify master tokens (24h) |
| `app.jwt.secret-session` | `APP_JWT_SECRET_SESSION` | Sign/verify session tokens (15m) |

- Keys must be long enough for JJWT HMAC (≥ 256-bit / 32+ chars). Fail at startup if weak.
- Master token **hashed SHA-256 (Base64)** before DB / Feign persistence — never store raw master JWT in PARAMETERS.
- Session token returned to client; also stored on `User` for ops visibility; **revocation** is via `sessionUuid`.

## Session validity (defense in depth)

A session is valid only if:

1. JWT signature + expiry OK (`JwtUtil.isSessionTokenValid`)
2. Claim `uuid` present and `UserRepository.findBySessionUuid` hits

Used by: `JwtAuthFilter`, `AuthServiceImpl.isSessionTokenValid`. Renew rotates UUID → old JWT dies without blacklist.

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
| Cryptographic failures | BCrypt(10); dual secrets; no default prod secrets |
| Injection | JPA params; validation on DTOs |
| Security misconfig | Swagger/H2 off by default; profile must be explicit |
| Auth failures | Lockout + rate limit + generic errors |
| Sensitive data | `Cache-Control: no-store` on login/renew; no password in responses |

## CORS

`APP_CORS_ALLOWED_ORIGINS` comma-separated. Credentials false. Tighten in prod.

## Out of scope (do not fake)

- Active Directory / LDAP (see ACTIVE-DIRECTORY.md)
- Redis-backed rate limit / token blacklist
- Spring Boot 3 security rewrite
