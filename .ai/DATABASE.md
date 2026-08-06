# DATABASE — H2 / Postgres / Flyway / User

> Dev/test: in-memory H2 (Flyway **off**, Hibernate DDL). Prod: PostgreSQL + Flyway (`ddl-auto=validate`).

## Profiles

| Profile | Datasource | `ddl-auto` | Flyway |
|---------|------------|------------|--------|
| `dev` | H2 mem `authdb` | `update` | `false` |
| `test` | H2 mem `authdb` | `create-drop` | `false` |
| `prod` | `${SPRING_DATASOURCE_URL}` (Postgres) | `validate` | `true` |

Base `application.properties` does **not** pin a sole H2 strategy — datasource lives in profile files.

H2 console: off by default; `dev` enables. Never expose console in prod.

## Flyway

- Migration: `src/main/resources/db/migration/V1__create_users.sql`
- Matches `User` → `USERS` (all columns, unique username/email, index on `SESSION_UUID`).
- SQL targets PostgreSQL. H2 does not run Flyway in this project (disabled in dev/test).

## Entity highlights (`User`)

| Column | Role |
|--------|------|
| `USERNAME` / `EMAIL` | Unique |
| `PASSWORD_HASH` | BCrypt |
| `ROLE` | Enum string: `ADMIN`, `USER`, `READONLY` |
| `MASTER_TOKEN` | SHA-256 (Base64) hash of master JWT |
| `SESSION_TOKEN` | SHA-256 (Base64) hash of session JWT — **not** the plaintext JWT |
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

- Prod schema is owned by Flyway; Hibernate only validates.
- Register path: `POST /api/users` (ADMIN) via `UserService` — uniqueness → `ConflictException` 409.
- Do not return `passwordHash` / tokens in list DTOs (`UserSummaryDto`).
