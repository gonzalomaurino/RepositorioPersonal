package utnfc.isi.back.sim.infra;

import org.h2.tools.RunScript;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Ejecuta el DDL provisto (SIN MODIFICAR) desde resources/sql/ddl_legos.sql
 * contra la misma URL H2 que usa JPA/Hibernate.
 */
public final class DatabaseInitializer {
    private DatabaseInitializer(){}

    // Debe coincidir EXACTO con persistence.xml
    private static final String URL  = "jdbc:h2:mem:backdb;DB_CLOSE_DELAY=-1;MODE=LEGACY";
    private static final String USER = "sa";
    private static final String PASS = "";

    private static final String DDL_CLASSPATH = "/sql/ddl.sql";

    public static void recreateSchemaFromDdl() {
        try (Connection cn = DriverManager.getConnection(URL, USER, PASS)) {
            var in = DatabaseInitializer.class.getResourceAsStream(DDL_CLASSPATH);
            if (in == null) {
                throw new IllegalStateException("No se encontró " + DDL_CLASSPATH + " en el classpath.");
            }
            try (var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                RunScript.execute(cn, reader);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error ejecutando DDL: " + DDL_CLASSPATH, e);
        }
    }
}
