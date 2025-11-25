package utnfc.isi.back.sim;

import org.junit.jupiter.api.Test;
import utnfc.isi.back.sim.infra.DatabaseInitializer;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class SchemaSmokeTest {

    private static final String URL  = "jdbc:h2:mem:backdb;DB_CLOSE_DELAY=-1;MODE=LEGACY";
    private static final String USER = "sa";
    private static final String PASS = "";

    @Test
    void estructuraBasicaCreada() throws Exception {
        // inicializa explícitamente desde DDL (idempotente para este test)
        DatabaseInitializer.recreateSchemaFromDdl();

        try (var cn = DriverManager.getConnection(URL, USER, PASS)) {
            // Tablas
            assertTrue(existsTable(cn, "DESARROLLADORES"));
            assertTrue(existsTable(cn, "GENEROS"));
            assertTrue(existsTable(cn, "PLATAFORMAS"));
            assertTrue(existsTable(cn, "JUEGOS"));

            // Columnas principales
            assertTrue(existsColumn(cn, "DESARROLLADORES", "DESA_ID"));
            assertTrue(existsColumn(cn, "GENEROS", "GEN_ID"));
            assertTrue(existsColumn(cn, "PLATAFORMAS", "PLAT_ID"));
            assertTrue(existsColumn(cn, "JUEGOS", "JUEGO_ID"));
            assertTrue(existsColumn(cn, "JUEGOS", "GENERO_ID"));
            assertTrue(existsColumn(cn, "JUEGOS", "DESARROLLADOR_ID"));
            assertTrue(existsColumn(cn, "JUEGOS", "PLATAFORMA_ID"));
            assertTrue(existsColumn(cn, "JUEGOS", "CLASIFICACION_ESRB"));

            // Secuencias
            assertTrue(existsSequence(cn, "SEQ_DESARROLLADORES"));
            assertTrue(existsSequence(cn, "SEQ_GENEROS"));
            assertTrue(existsSequence(cn, "SEQ_PLATAFORMAS"));
            assertTrue(existsSequence(cn, "SEQ_JUEGOS"));
        }
    }

    private boolean existsTable(java.sql.Connection cn, String table) throws Exception {
        try (var ps = cn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?")) {
            ps.setString(1, table);
            try (var rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    private boolean existsColumn(java.sql.Connection cn, String table, String column) throws Exception {
        try (var ps = cn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?")) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (var rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    private boolean existsSequence(java.sql.Connection cn, String seq) throws Exception {
        try (var ps = cn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = ?")) {
            ps.setString(1, seq);
            try (var rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }
}

