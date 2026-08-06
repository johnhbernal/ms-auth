# QA — ms-auth (lecciones Kilele)

## Mandato

- JUnit/MockMvc **prueban contrato AuthZ** (`@PreAuthorize`, JWT claims).
- **No** acreditan UI Filament/React — eso es Playwright en `ms-frontend`.
- Cobertura JaCoCo ≥ 0.70 en `verify` — techo honesto; no claim 100%.

## Suites canónicas AuthZ

| Clase | Qué prueba |
|-------|------------|
| `RbacControllerIntegrationTest` | list/create RBAC; seller GET inventario 200 + PUT precio/stock **403**; admin write OK; body inválido **400** (`@Valid`) |
| `FlywayMigrationFilesTest` | gate pragmático V4/V5 (MODULE + INVENTARIO_*); Testcontainers = residual opcional |
| `PasswordResetIntegrationTest` | forgot/reset one-time token; admin reset |
| `AuthorityResolutionServiceImplIntegrationTest` | groups → roles → permissions en seed |

## Reglas

1. Constraints Bean Validation en **interface** o **impl**, no redefinir (HV000151).
2. Entidades M2M: excluir colecciones de `equals/hashCode` (StackOverflow).
3. Seed `dev`/`stack`: seller + INVENTARIO_* + sync ADMIN permissions.
4. Tras cambiar AuthZ: correr integración **y** pedir E2E frontend.

## Comando

```powershell
.\mvnw.cmd -B "-Dtest=RbacControllerIntegrationTest,FlywayMigrationFilesTest,PasswordResetIntegrationTest" test
.\mvnw.cmd -B verify   # + JaCoCo gate
```
