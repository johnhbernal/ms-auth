# SPRINGBOOT — Boot 2.7 patterns (ms-auth)

> Spring Boot **2.7.18** · Spring Security filter chain · OpenFeign · springdoc 1.7

## Profiles

| Profile | Purpose |
|---------|---------|
| *(none)* | Base `application.properties` — requires `APP_JWT_SECRET_*` env |
| `dev` | Local secrets, H2 console, Swagger, `DataInitializer` seed users |
| `test` | Test overrides; combine with `dev` in IT for seeds |

**Never** hardcode `spring.profiles.active=dev` in base properties. Local run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Security filter order (conceptual)

1. `RateLimitFilter` (`@Order(HIGHEST_PRECEDENCE)`) — login IP bucket
2. Spring Security chain → `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`
3. MVC `@PreAuthorize` + `GlobalExceptionHandler` for `AccessDeniedException`

Public matchers live in `SecurityConfig.PUBLIC_ENDPOINTS`. Method security: `@EnableGlobalMethodSecurity(prePostEnabled = true)`.

## API contract

- Envelope: `ApiResponse` (`code`, `description`, `data`).
- Validation: `@Valid` on bodies; `@Validated` + `@NotBlank` on query params → `ConstraintViolationException` → 400.
- Stateless: `SessionCreationPolicy.STATELESS`; CSRF off; CORS from `app.cors.allowed-origins`.

## Feign

- `PracticaServiceClient` → `ms-practica.url`.
- Login **must continue** if Feign fails (log only).
- Integration tests: `@MockBean PracticaServiceClient`.

## OpenAPI

Disabled unless `SWAGGER_ENABLED=true` or `dev` profile enables springdoc. Do not expose Swagger in prod.

## Package / plugin

`spring-boot-maven-plugin` packages executable JAR. Dockerfile builds from that artifact.
