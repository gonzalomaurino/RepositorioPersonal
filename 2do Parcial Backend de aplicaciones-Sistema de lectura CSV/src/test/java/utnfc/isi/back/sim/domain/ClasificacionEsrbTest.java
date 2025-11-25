package utnfc.isi.back.sim.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utnfc.isi.back.sim.infra.DatabaseInitializer;
import utnfc.isi.back.sim.infra.LocalEntityManagerProvider;

import static org.junit.jupiter.api.Assertions.*;

class ClasificacionEsrbTest {

    private static EntityManager em;

    @BeforeAll
    static void init() {
        // recrea la BD y arranca EM
        DatabaseInitializer.recreateSchemaFromDdl();
        em = LocalEntityManagerProvider.em();
    }

    @AfterAll
    static void tearDown() {
        if (em != null && em.isOpen()) em.close();
        LocalEntityManagerProvider.close();
    }

    @Test
    void testFromCodigo() {
        assertEquals(ClasificacionEsrb.E, ClasificacionEsrb.fromCodigo("E"));
        assertEquals(ClasificacionEsrb.E10, ClasificacionEsrb.fromCodigo("E10+"));
        assertEquals(ClasificacionEsrb.M, ClasificacionEsrb.fromCodigo("M"));
        assertThrows(IllegalArgumentException.class, () -> ClasificacionEsrb.fromCodigo("XYZ"));
    }

    @Test
    void testPersistenciaEnum() {
        for (ClasificacionEsrb c : ClasificacionEsrb.values()) {
            em.getTransaction().begin();

            Juego j = Juego.builder()
                    .titulo("Juego_" + c.getCodigo())
                    // .clasificacionEsrb(c)
                    .resumen("Demo persistencia " + c.getNombre())
                    .build();
            j.setClasificacionEsrb(c);

            em.persist(j);
            em.getTransaction().commit();

            // Verificar lectura desde la BD
            Juego reloaded = em.find(Juego.class, j.getId());
            assertNotNull(reloaded, "El juego debería haberse persistido");
            assertEquals(c, reloaded.getClasificacionEsrb(),
                    "La clasificación ESRB debería conservarse tras persistir y leer");
        }
    }
}

