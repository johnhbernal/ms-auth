# JAVA — ms-auth conventions

> Java **17** · Maven · package root `co.com.practica.auth`

## Layout

```
src/main/java/co/com/practica/auth/
  config/       Security, JWT filter, rate limit, seed
  controller/   Interfaces + impl/
  dto/          Request/response + ApiResponse envelope
  entity/       JPA User
  enums/        Role
  exception/    Domain + GlobalExceptionHandler
  repository/   Spring Data
  service/      Interfaces + impl/
  util/         JwtUtil, Feign client
  constants/    AppConstants (no magic strings)
```

## Rules

- Public APIs: typed DTOs; no entity leak on write paths that expose secrets.
- Controllers thin; business logic in services with `@Transactional` where state changes.
- Prefer constructor injection (`@RequiredArgsConstructor`).
- Lombok OK (`@Data`, `@Builder`, `@Log4j2`); do not fight the existing style.
- Methods short; extract private helpers for login/renew flows.
- Tests: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`); integration with `@SpringBootTest` + MockMvc.
- London-school TDD for unit tests: mock collaborators; assert interactions on auth/DB boundaries.

## Dependencies (pinned via parent)

- Spring Boot parent `2.7.18` → `javax.*` servlet/validation (not Jakarta).
- JJWT `0.11.5` (`io.jsonwebtoken`).
- Bucket4j `7.6.0` GAV: `com.github.vladimir-bukhtoyarov:bucket4j-core` (package `io.github.bucket4j`).

## Do not

- Bump to Spring Boot 3 / Jakarta in a drive-by change.
- Add unused libraries or duplicate constants outside `AppConstants`.
