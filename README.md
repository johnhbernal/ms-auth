# ms-auth

Production-oriented authentication microservice template for the Practica system (stateless JWT gateway).

## Stack

| | |
|---|---|
| Runtime | Java 17 |
| Framework | Spring Boot **2.7.18** |
| Build | Maven Wrapper (`./mvnw`) |
| DB (dev/test) | H2 in-memory (Flyway off, Hibernate DDL) |
| DB (prod) | PostgreSQL 16 + Flyway (`ddl-auto=validate`) |
| JWT | JJWT 0.11.5 · dual secrets (master / session) |
| Security | Spring Security · BCrypt · Bucket4j rate limit |
| Observability | Actuator (`/actuator/health` only) |
| Coverage | JaCoCo line coverage **≥ 0.70** on `verify` |

## Profiles

| Profile | Datasource | DDL | Flyway | Notes |
|---------|------------|-----|--------|-------|
| `dev` | H2 mem | `update` | off | Swagger + H2 console; seed users; JWT secrets in properties |
| `test` | H2 mem | `create-drop` | off | JWT test secrets; used by `@ActiveProfiles({"dev","test"})` |
| `prod` | Postgres via env | `validate` | **on** | Swagger/H2 off; JWT + DB from env |

Do **not** set `spring.profiles.active=dev` in base `application.properties`. Activate explicitly.

## Quick start (local)

```bash
# Toolchain (Windows Scoop): see scripts/setup-local.ps1
# Dev server (H2 + seed users)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# → http://localhost:8081
# → Swagger: http://localhost:8081/swagger-ui/index.html
```

## IDE setup (IntelliJ / Cursor)

False positives like `Cannot resolve symbol 'String'`, “method is never used” on Spring mappings, or typo on `practica` mean the **project JDK is missing** — not OWASP gaps.

| IDE | Fix |
|-----|-----|
| **IntelliJ** | `powershell -File scripts/setup-intellij-sdk.ps1` → restart IDE → **File → Project Structure → Project → SDK = `temurin-17`**. Or click the yellow **Setup SDK** banner and pick that JDK (`%USERPROFILE%\scoop\apps\temurin17-jdk\current`). |
| **Cursor / VS Code** | Already tracked: `.vscode/settings.json` points `java.jdt.ls.java.home` at Scoop Temurin 17; dictionary includes `practica`. |

Project language level is committed in `.idea/misc.xml` (`JDK_17` / `temurin-17`).

## Docker Compose (prod-like)

```bash
cp .env.example .env   # set APP_JWT_* (≥32 chars) and DB password
docker compose up --build
# → http://localhost:8081/actuator/health
```

Services: `postgres` (16-alpine, volume, healthcheck) + `ms-auth` (depends on healthy Postgres, profile `prod`).

### Full stack (auth + practica + frontend)

See sibling folder [`../practica-stack`](../practica-stack) — one Postgres (two DBs), all three services, seed users via profile `prod,stack`.

```powershell
cd ..\practica-stack
copy .env.example .env
powershell -File scripts\smoke-stack-docker.ps1
```

## Local CI

```bash
./mvnw -B verify          # tests + JaCoCo gate (≥ 70% line)
# Windows helper:
powershell -File scripts/ci-local.ps1
```

## Environment variables

| Variable | Required | Description |
|----------|----------|-------------|
| `APP_JWT_SECRET_MASTER` | prod | HMAC secret ≥ 32 bytes |
| `APP_JWT_SECRET_SESSION` | prod | HMAC secret ≥ 32 bytes |
| `SPRING_DATASOURCE_URL` | prod | e.g. `jdbc:postgresql://host:5432/msauth` |
| `SPRING_DATASOURCE_USERNAME` | prod | DB user |
| `SPRING_DATASOURCE_PASSWORD` | prod | DB password |
| `APP_CORS_ALLOWED_ORIGINS` | optional | Comma-separated origins (default `http://localhost:3000`) |
| `MS_PRACTICA_URL` | optional | Feign base URL (default `http://localhost:8082`) |
| `SPRING_PROFILES_ACTIVE` | docker | Use `prod` |
| `JAVA_OPTS` | optional | JVM flags |

See `.env.example` for a full template.

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | — | Authenticate → sessionToken (15 min) |
| POST | `/api/auth/renew` | — | Renew sessionToken |
| POST | `/api/auth/logout` | Bearer | Revoke session (nulls UUID) |
| GET | `/api/auth/validate` | — | Check token validity |
| GET | `/api/users` | Bearer ADMIN | List users |
| POST | `/api/users` | Bearer ADMIN | Register user |
| GET | `/actuator/health` | — | Liveness/health (no details) |

## Security notes

- JWT secrets must be ≥ 32 bytes; `JwtUtil` fails fast at startup if missing/short.
- Passwords: BCrypt (strength 10).
- **Master** and **session** tokens are stored as SHA-256 (Base64) hashes in `USERS` — never plaintext JWT.
- Session validity = signature OK **and** `SESSION_UUID` still present in DB (logout/renew revoke without blacklist).
- Account lockout: 5 failed logins → 15 min lock.
- Rate limit: 10 login attempts/min/IP (Bucket4j, in-memory).
- Actuator exposes **health only**; `show-details=never`.
- Swagger / H2 console off outside `dev`.

## Seed users (dev only)

| User | Password | Role |
|------|----------|------|
| admin | Admin123! | ADMIN |
| user | User123! | USER |
| reader | Read123! | READONLY |

Loaded by `DataInitializer` when profile `dev` is active. **Not for production.**

## Agent docs

See [`.ai/AGENTS.md`](.ai/AGENTS.md) for security, database, and CI specialist notes.
