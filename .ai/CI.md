# CI — GitHub Actions gates

> Workflow: `.github/workflows/ci.yml` · triggers: push/PR to `main`

## Jobs (ordered)

| Job | Gate |
|-----|------|
| **Build** | `./mvnw -B package -DskipTests` · Java 17 Temurin · upload JAR artifact |
| **Test** | needs Build · `./mvnw -B verify` (tests + JaCoCo line coverage **≥ 0.70**) |
| **Docker Build** | needs Test · `docker build .` |

Env: `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true` (Node 20 action runtime deprecation mitigation).

Actions in use: `actions/checkout@v4`, `actions/setup-java@v4`, `actions/upload-artifact@v4`.

## Local parity

```bash
./mvnw -B verify
./mvnw -B package -DskipTests
docker build .
# Windows: powershell -File scripts/ci-local.ps1
```

CI runs without forcing `spring.profiles.active`; tests use `@ActiveProfiles({"dev","test"})` and `application-test.properties` (H2 + JWT test secrets).

## Coverage

JaCoCo `prepare-agent` / `report` / `check` bound to `verify` with minimum line `COVEREDRATIO` **0.70**.

## Known CI breakers (fixed / watch)

| Issue | Fix |
|-------|-----|
| Bucket4j GAV `com.bucket4j:bucket4j-core` missing | Use `com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0` |
| JWT filter tests ignore UUID/DB | Mock `UserRepository.findBySessionUuid` |
| `isSessionTokenValid` unit test | Mock claims UUID + repository |
| Login IT password `< 8` chars | Respect `@Size(min=8)` on `LoginRequest` |
| Renew before validate IT | `@Order` + refresh shared token after renew |
| Hardcoded `spring.profiles.active=dev` | Explicit `-Dspring-boot.run.profiles=dev` locally |
| Weak/missing JWT secrets outside profiles | Fail-fast in `JwtUtil.init()`; test/dev properties supply ≥32-char secrets |

## Policy

- Do not skip tests on the Test job (`-DskipTests` only on Build job by design).
- Do not commit secrets; JWT env required outside `dev`/`test` seeds.
- Keep Boot on 2.7.x unless a dedicated upgrade epic exists.
- Prefer Maven Wrapper (`./mvnw`) over a host Maven install.
- **After every push:** open the Actions run. If red → read logs → fix → local verify → push again. Never assume local green alone.

## Distinguishing failure types

| Symptom | Cause | Action |
|---------|--------|--------|
| Annotation: *job was not acquired by Runner…* (~15 min) | GitHub hosted-runner capacity / queue — **not a code bug** | `gh run rerun <id> --failed`; wait; check [GitHub Status](https://www.githubstatus.com/) |
| Job starts, Maven/npm step fails with ERROR logs | Real CI gate | Reproduce locally (`./mvnw verify` / `npm ci`) and fix |
| Queued forever / cancelled with no logs | Infra / billing / Actions outage | Do not “fix code”; re-run when runners available |
