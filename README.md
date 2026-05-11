# ms-auth

Authentication microservice for the Practica system.

## Stack

| | |
|---|---|
| Runtime | Java 17 |
| Framework | Spring Boot 2.7.18 |
| Database | H2 in-memory |
| JWT library | JJWT 0.11.5 |

## How to run

```bash
# Required environment variables
export APP_JWT_SECRET_MASTER=<at-least-32-char-secret>
export APP_JWT_SECRET_SESSION=<at-least-32-char-secret>

mvn spring-boot:run
# Server starts on http://localhost:8081
```

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | — | Authenticate, receive sessionToken (15 min) |
| POST | `/api/auth/renew` | — | Renew sessionToken using current token |
| POST | `/api/auth/logout` | Bearer | Revoke session (nulls UUID in DB) |
| GET | `/api/auth/validate` | Bearer | Check if current token is still valid |
| GET | `/api/users` | Bearer ADMIN | List users (paginated) |
| POST | `/api/users/register` | Bearer ADMIN | Register a new user |

## Token model

- **sessionToken** (15 min) — sent with every API request as `Authorization: Bearer <token>`
- Session UUID stored in DB; logout invalidates immediately without a blacklist
- Account locks for 15 min after 5 consecutive failed logins

## Security notes

- JWT secrets must be ≥ 32 bytes; the app fails fast at startup otherwise
- Passwords hashed with BCrypt (strength 10)
- masterToken hash stored with SHA-256 before persistence
- Rate limiting: 10 login attempts/min per IP (Bucket4j, in-memory)
- CORS origins configured via `APP_CORS_ALLOWED_ORIGINS` (comma-separated)
- Swagger UI disabled by default; enable with `SWAGGER_ENABLED=true` in dev only

## Swagger UI (dev)

```bash
SWAGGER_ENABLED=true mvn spring-boot:run
# http://localhost:8081/swagger-ui/index.html
```
