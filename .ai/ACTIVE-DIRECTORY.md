# ACTIVE-DIRECTORY — simulated RBAC (implemented)

> **Portfolio / practice project.** Real LDAP is **not** wired. This module teaches AuthN vs AuthZ using AD-style naming (`distinguishedName`, `memberOf`) backed by PostgreSQL/H2 tables.

## AuthN vs AuthZ

| Layer | Question | ms-auth implementation |
|-------|----------|------------------------|
| **Authentication (AuthN)** | Who are you? | BCrypt password check (`local`) or `SimulatedDirectoryService.bind()` (`directory` mode). Same users/passwords; directory mode logs DN + `memberOf`. |
| **Authorization (AuthZ)** | What may you do? | `User` → `DirectoryGroup` → `AppRole` → `Permission(module, code)`. JWT carries `role`, `roles[]`, `permissions[]`, `groups[]`. Spring authorities: `ROLE_*`, `PERM_*`. |

## Module-scoped permissions (complete demo)

Permissions are tagged with a logical **module** (`INVENTARIO`, `PARAMETROS`, `RBAC`, …). Example matrix:

| Permission | Module | VENDEDOR | ADMIN |
|------------|--------|----------|-------|
| `INVENTARIO_PRECIO_READ` | INVENTARIO | ✅ | ✅ |
| `INVENTARIO_PRECIO_WRITE` | INVENTARIO | ❌ | ✅ |
| `INVENTARIO_STOCK_WRITE` | INVENTARIO | ❌ | ✅ |

Demo API (enforced with `@PreAuthorize`):

| Method | Path | Authority |
|--------|------|-----------|
| GET | `/api/demo/inventario/productos` | `PERM_INVENTARIO_PRECIO_READ` or `ROLE_ADMIN` |
| PUT | `/api/demo/inventario/productos/precio` | `PERM_INVENTARIO_PRECIO_WRITE` or `ROLE_ADMIN` |
| PUT | `/api/demo/inventario/productos/stock` | `PERM_INVENTARIO_STOCK_WRITE` or `ROLE_ADMIN` |

Admin UI (ms-frontend `/admin/rbac`) can **create** permissions (with module), roles, groups, assign memberships, and attach/detach permissions — not only list seed data.

## Configuration

```properties
# application.properties
app.auth.mode=${APP_AUTH_MODE:local}
```

| Mode | Behaviour |
|------|-----------|
| `local` (default) | `UserRepository` + BCrypt + account lockout |
| `directory` | `SimulatedDirectoryService.bind()` then identical session JWT issuance |

Real AD would replace `SimulatedDirectoryServiceImpl` with `LdapContext` / StartTLS — **out of scope** until product approval.

## Data model

```
User ──memberOf──► DirectoryGroup ──grants──► AppRole ──includes──► Permission(module)
  │
  └── role (enum ADMIN/USER/READONLY) — legacy primary role for ms-practica Feign
```

| Entity | Table | Notes |
|--------|-------|-------|
| `Permission` | `PERMISSIONS` | `CODE` + `MODULE` (Flyway V2/V4) |
| `AppRole` | `APP_ROLES` | Aggregates permissions (ADMIN, VENDEDOR, …) |
| `DirectoryGroup` | `DIRECTORY_GROUPS` | AD-style `distinguishedName`, grants `AppRole`s |
| M2M | `USER_GROUPS`, `GROUP_ROLES`, `ROLE_PERMISSIONS` | Flyway V2 (Postgres prod) |

## Seed matrix (profiles `dev`, `stack`)

| User | Password | User.role | Group | AppRole | Key permissions |
|------|----------|-----------|-------|---------|-----------------|
| admin | Admin123! | ADMIN | G-Admins | ADMIN | all |
| user | User123! | USER | G-Operators | OPERATOR | PARAMETRO_*, DIRECTORY_READ |
| reader | Read123! | READONLY | G-Readers | READONLY | PARAMETRO_READ |
| seller | Seller123! | USER | G-Vendors | VENDEDOR | `INVENTARIO_PRECIO_READ` only |

## JWT claims (session token)

| Claim | Example |
|-------|---------|
| `role` | `ADMIN` (primary — ms-practica compat) |
| `roles` | `["ADMIN","OPERATOR",…]` |
| `permissions` | `["PARAMETRO_READ","INVENTARIO_PRECIO_READ",…]` (bare codes; Spring adds `PERM_`) |
| `groups` | `["G-Admins",…]` |

## Password reset (no plaintext email)

| Flow | Path | Notes |
|------|------|-------|
| Self-service forgot | `POST /api/auth/forgot-password` | One-time token; generic response; token logged in `dev`/`stack` |
| Self-service reset | `POST /api/auth/reset-password` | Consumes token; sets BCrypt hash |
| Admin reset | `POST /api/users/{id}/reset-password` | Admin AuthZ; revokes session |

Never emails the new password in clear text.

## APIs

| Method | Path | Access |
|--------|------|--------|
| GET | `/api/directory/me` | Authenticated — DN, memberOf, roles, permissions |
| GET/POST | `/api/rbac/permissions` | Read: `DIRECTORY_READ`; Create: `GROUP_ADMIN` |
| GET/POST | `/api/rbac/roles` | Read: `DIRECTORY_READ`; Create: `GROUP_ADMIN` |
| GET/POST | `/api/rbac/groups` | Read: `DIRECTORY_READ`; Create: `GROUP_ADMIN` |
| POST/DELETE | `/api/rbac/groups/{id}/members/{userId}` | `GROUP_ADMIN` |
| POST | `/api/rbac/groups/{id}/roles/{roleName}` | `GROUP_ADMIN` |
| POST/DELETE | `/api/rbac/roles/{roleName}/permissions/{permCode}` | `GROUP_ADMIN` |
| GET/PUT | `/api/demo/inventario/**` | Module permissions above |

## Future LDAP migration (not implemented)

1. Add `spring-boot-starter-data-ldap` behind feature flag.
2. Replace `SimulatedDirectoryServiceImpl` with LDAP bind provider.
3. Keep JWT + `sessionUuid` revocation model unchanged.
4. Provision/sync local `User` row on first LDAP login.

## Explicit non-goals

- No AD dependency in `pom.xml`.
- No corporate DN schema without customer input.
- No Redis-backed session store.
