# Project Memory

Shared team memory. Committed to git. Updated automatically by /checkpoint.

## Stack
<!-- Set by /stack -->

## Architecture Decisions
<!-- Append by /checkpoint. Never delete entries. -->

## Active Conventions
<!-- Project-specific conventions established in this codebase -->

## Technical Debt
<!-- Known shortcuts, limitations, and deferred work -->

## Workarounds
<!-- Non-obvious solutions and why they exist -->

## Spec: github-actions-ci 2026-05-06

Three-job GitHub Actions pipeline (build → test → docker-build) triggered on push/PR to `main`. No secrets required — JWT defaults in `application.properties` cover test runs. Docker image built but not pushed.

## Spec: integration-tests 2026-05-05

`@SpringBootTest` + `@AutoConfigureMockMvc` integration tests for the full filter chain and auth endpoints. Single test class `AuthControllerIntegrationTest` with 11 tests. Uses `@ActiveProfiles({"dev","test"})` to activate DataInitializer seed data and load `application-test.properties`. `@MockBean PracticaServiceClient` prevents Feign from calling ms-practica during login. Covers: login (happy path, wrong password, blank fields), renew, validate, and JWT filter chain on a protected endpoint (no token→401, valid token→not 401, tampered→401).
