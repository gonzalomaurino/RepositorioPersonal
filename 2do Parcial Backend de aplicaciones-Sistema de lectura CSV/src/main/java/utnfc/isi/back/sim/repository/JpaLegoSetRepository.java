package utnfc.isi.back.sim.repository;

import jakarta.persistence.EntityManager;
import utnfc.isi.back.sim.domain.LegoSet;
import utnfc.isi.back.sim.infra.LocalEntityManagerProvider;

import java.util.List;
import java.util.stream.Collectors;

public class JpaLegoSetRepository {

    private final EntityManager em = LocalEntityManagerProvider.em();

    /**
     * Retorna los 5 países con menor relación costo/valoración promedio.
     * Se calcula como promedio(list_price / star_rating) agrupado por país.
     */
    public List<Object[]> rankingPaisesPorCostoValoracion() {
        return em.createQuery("""
            SELECT s.country.code, AVG(s.listPrice / s.starRating)
            FROM LegoSet s
            WHERE s.starRating IS NOT NULL AND s.listPrice IS NOT NULL
            GROUP BY s.country.code
            HAVING COUNT(s.id) > 0
            ORDER BY AVG(s.listPrice / s.starRating) ASC
        """, Object[].class)
        .setMaxResults(5)
        .getResultList();
    }

    /**
     * Lista de sets disponibles para una edad específica,
     * con precio menor a un valor máximo y valoración >= 4.8.
     * El filtrado por rango de edad se hace en memoria usando matchesAge().
     */
    public List<LegoSet> findByEdadPrecioValoracion(int edad, double precioMax) {
        // Trae todos los sets con referencias
        var all = em.createQuery("""
            SELECT s FROM LegoSet s
            JOIN FETCH s.ageGroup
            JOIN FETCH s.country
            JOIN FETCH s.theme
        """, LegoSet.class).getResultList();

        // Filtra en memoria usando matchesAge y condiciones del enunciado
        return all.stream()
                .filter(s -> s.getAgeGroup() != null && s.getAgeGroup().matchesAge(edad))
                .filter(s -> s.getListPrice() != null && s.getListPrice().doubleValue() < precioMax)
                .filter(s -> s.getStarRating() != null && s.getStarRating() >= 4.8)
                .sorted((a, b) -> b.getListPrice().compareTo(a.getListPrice())) // mayor a menor precio
                .collect(Collectors.toList());
    }

    /**
     * Devuelve todos los sets con sus relaciones cargadas.
     * Útil para pruebas o verificación general.
     */
    public List<LegoSet> findAllWithRefs() {
        return em.createQuery("""
            SELECT s FROM LegoSet s
            JOIN FETCH s.country
            JOIN FETCH s.theme
            JOIN FETCH s.ageGroup
        """, LegoSet.class).getResultList();
    }
}
