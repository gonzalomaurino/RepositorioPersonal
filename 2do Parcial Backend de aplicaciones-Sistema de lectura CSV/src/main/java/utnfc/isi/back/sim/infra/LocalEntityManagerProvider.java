package utnfc.isi.back.sim.infra;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/** Proveedor local de EntityManager. */
public final class LocalEntityManagerProvider {
    private static final String PU = "pu-backend";
    private static final EntityManagerFactory EMF;

    static {
        DatabaseInitializer.recreateSchemaFromDdl();
        EMF = Persistence.createEntityManagerFactory(PU);
    }
    private LocalEntityManagerProvider(){}

    public static EntityManager em() { return EMF.createEntityManager(); }
    public static void close() { EMF.close(); }
}
