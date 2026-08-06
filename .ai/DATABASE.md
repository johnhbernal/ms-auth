# DATABASE — H2 / JPA / User

> In-memory H2 for dev/test. JPA entity `User` → table `USERS`.

## Datasource (base)

```
jdbc:h2:mem:authdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
ddl-auto=create-drop
open-in-view=false
```

H2 console: off by default; `dev` enables. Never expose console in prod.

## Entity highlights (`User`)

| Column | Role |
|--------|------|
| `USERNAME` / `EMAIL` | Unique |
| `PASSWORD_HASH` | BCrypt |
| `ROLE` | Enum string: `ADMIN`, `USER`, `READONLY` |
| `MASTER_TOKEN` | SHA-256 hash of master JWT |
| `SESSION_TOKEN` | Last issued session JWT (TEXT) |
| `SESSION_UUID` | Active session id (claim `uuid`); null = logged out |
| `STATUS` | `A` / `I` |
| `FAILED_LOGIN_ATTEMPTS` / `LOCKED_UNTIL` | Lockout |
| `SESSION_TOKEN_EXPIRES_AT` / `LAST_LOGIN_AT` | Ops metadata |

`@PrePersist` sets `createdAt` if missing.

## Repository

`UserRepository`: `findByUsernameAndStatus`, `findBySessionUuid`, `existsByUsername`, paging for admin list.

## Seed (`DataInitializer`, `@Profile("dev")`)

| User | Password | Role |
|------|----------|------|
| admin | Admin123! | ADMIN |
| user | User123! | USER |
| reader | Read123! | READONLY |

## Notes

- Schema is create-drop — not for durable prod data; swap datasource + `ddl-auto` when promoting.
- Register path: `POST /api/users` (ADMIN) via `UserService` — uniqueness → `ConflictException` 409.
- Do not return `passwordHash` / tokens in list DTOs (`UserSummaryDto`).
