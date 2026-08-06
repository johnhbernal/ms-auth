# CI — GitHub Actions gates

> Workflow: `.github/workflows/ci.yml` · triggers: push/PR to `main`

## Jobs (ordered)

| Job | Gate |
|-----|------|
| **Build** | `mvn package -DskipTests` · Java 17 Temurin · upload JAR artifact |
| **Test** | needs Build · `mvn test` |
| **Docker Build** | needs Test · `docker build .` |

Env: `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true` (Node 20 action runtime deprecation mitigation).

Actions in use: `actions/checkout@v4`, `actions/setup-java@v4`, `actions/upload-artifact@v4`.

## Local parity (when Maven available)

```bash
mvn test
mvn package -DskipTests
docker build .
```

CI runs without forcing `spring.profiles.active`; tests use `@ActiveProfiles({"dev","test"})` and `application-test.properties`.

## Known CI breakers (fixed / watch)

| Issue | Fix |
|-------|-----|
| Bucket4j GAV `com.bucket4j:bucket4j-core` missing | Use `com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0` |
| JWT filter tests ignore UUID/DB | Mock `UserRepository.findBySessionUuid` |
| `isSessionTokenValid` unit test | Mock claims UUID + repository |
| Login IT password `< 8` chars | Respect `@Size(min=8)` on `LoginRequest` |
| Renew before validate IT | `@Order` + refresh shared token after renew |
| Hardcoded `spring.profiles.active=dev` | Explicit `-Dspring-boot.run.profiles=dev` locally |

## Policy

- Do not skip tests (`-DskipTests` only on Build job by design).
- Do not commit secrets; JWT env required outside `dev`/`test` seeds.
- Keep Boot on 2.7.x unless a dedicated upgrade epic exists.
