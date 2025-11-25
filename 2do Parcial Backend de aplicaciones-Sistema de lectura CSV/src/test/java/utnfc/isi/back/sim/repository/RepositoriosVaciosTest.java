package utnfc.isi.back.sim.repository;

import org.junit.jupiter.api.*;
import utnfc.isi.back.sim.infra.DatabaseInitializer;
import utnfc.isi.back.sim.infra.LocalEntityManagerProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de verificación de funcionamiento básico de los repositorios.
 * Asegura que la base fue creada correctamente, el EM funciona,
 * y todas las tablas comienzan vacías.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepositoriosVaciosTest {

    private static JpaDesarrolladorRepository repoDesa;
    private static JpaGeneroRepository repoGen;
    private static JpaPlataformaRepository repoPlat;
    private static JpaJuegoRepository repoJuego;

    @BeforeAll
    static void init() {
        // Recreatea la base en memoria a partir del DDL
        DatabaseInitializer.recreateSchemaFromDdl();

        // Instancia los repos
        repoDesa = new JpaDesarrolladorRepository();
        repoGen  = new JpaGeneroRepository();
        repoPlat = new JpaPlataformaRepository();
        repoJuego= new JpaJuegoRepository();
    }

    @AfterAll
    static void tearDown() {
        LocalEntityManagerProvider.close();
    }

    @Test @Order(1)
    void desarrolladoresArrancaVacio() {
        assertEquals(0, repoDesa.count(), "La tabla DESARROLLADORES debería estar vacía");
        assertTrue(repoDesa.findAll().isEmpty(), "findAll() debería devolver lista vacía");
    }

    @Test @Order(2)
    void generosArrancaVacio() {
        assertEquals(0, repoGen.count(), "La tabla GENEROS debería estar vacía");
        assertTrue(repoGen.findAll().isEmpty(), "findAll() debería devolver lista vacía");
    }

    @Test @Order(3)
    void plataformasArrancaVacio() {
        assertEquals(0, repoPlat.count(), "La tabla PLATAFORMAS debería estar vacía");
        assertTrue(repoPlat.findAll().isEmpty(), "findAll() debería devolver lista vacía");
    }

    @Test @Order(4)
    void juegosArrancaVacio() {
        assertEquals(0, repoJuego.count(), "La tabla JUEGOS debería estar vacía");
        assertTrue(repoJuego.findAll().isEmpty(), "findAll() debería devolver lista vacía");
    }
}

