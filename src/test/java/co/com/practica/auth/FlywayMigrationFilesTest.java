package co.com.practica.auth;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pragmatic Flyway gate without Testcontainers: migration files for module-scoped
 * RBAC must exist and mention INVENTARIO / module column. Runtime IT covers seed via H2.
 */
class FlywayMigrationFilesTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");

    @Test
    void moduleAndInventarioMigrationsArePresent() throws IOException {
        Path v4 = MIGRATIONS.resolve("V4__permission_module.sql");
        Path v5 = MIGRATIONS.resolve("V5__permission_module_inventario.sql");
        assertThat(v4).exists();
        assertThat(v5).exists();

        String v4Sql = Files.readString(v4, StandardCharsets.UTF_8);
        String v5Sql = Files.readString(v5, StandardCharsets.UTF_8);

        assertThat(v4Sql).containsIgnoringCase("module");
        assertThat(v5Sql).contains("INVENTARIO");
        assertThat(v5Sql).containsIgnoringCase("where code like 'inventario_%'");
    }
}
