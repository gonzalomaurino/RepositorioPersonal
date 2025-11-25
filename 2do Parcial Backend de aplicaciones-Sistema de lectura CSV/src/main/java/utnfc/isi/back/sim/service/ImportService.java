package utnfc.isi.back.sim.service;

import utnfc.isi.back.sim.csv.CsvLegoRow;
import utnfc.isi.back.sim.domain.*;
import utnfc.isi.back.sim.infra.LocalEntityManagerProvider;

import jakarta.persistence.EntityManager;
import lombok.*;

import java.util.*;

/**
 * Importa filas del CSV de LEGO en dos fases:
 *  1️⃣ Parseo y acumulación en memoria (Maps)
 *  2️⃣ Persistencia transaccional en BD (primero maestras, luego sets)
 *
 * Basado en la estructura del simulacro.
 */
public class ImportService {

    // ---------- Resultado de la importación ----------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public static class ImportResult {
        private int filasLeidas;
        private int filasValidas;
        private int countriesInsertados;
        private int themesInsertados;
        private int ageGroupsInsertados;
        private int setsInsertados;
    }

    // ---------- Método principal ----------
    public ImportResult importar(List<CsvLegoRow> rows) {
        Objects.requireNonNull(rows, "rows");

        Map<String, Country> countries = new LinkedHashMap<>();
        Map<String, Theme> themes = new LinkedHashMap<>();
        Map<String, AgeGroup> ages = new LinkedHashMap<>();
        List<PendingSet> pendings = new ArrayList<>();

        int validas = 0;

        for (var r : rows) {
            String themeName = trimOrNull(r.getThemeName());
            String ageCode = trimOrNull(r.getAges());
            String ctyCode = trimOrNull(r.getCountry());

            if (isBlank(themeName) || isBlank(ageCode) || isBlank(ctyCode) || r.getProdId() == null)
                continue;

            validas++;

            // Acumular maestras en memoria
            themes.computeIfAbsent(themeName, n -> Theme.builder().name(n).build());
            ages.computeIfAbsent(ageCode, c -> AgeGroup.builder().code(c).build());
            countries.computeIfAbsent(ctyCode, c -> Country.builder().code(c).name(c).build());

            // Buffer de sets pendientes
            pendings.add(PendingSet.builder()
                    .prodId(r.getProdId().intValue())
                    .setName(nvl(r.getSetName(), "(sin nombre)"))
                    .prodDesc(nvl(r.getProdDesc(), ""))
                    .reviewDifficulty(trimOrNull(r.getReviewDifficulty()))
                    .pieceCount(r.getPieceCount() != null ? r.getPieceCount().intValue() : null)
                    .starRating(r.getStarRating())
                    .listPrice(r.getListPrice() != null ? new java.math.BigDecimal(r.getListPrice()) : null)
                    .theme(themeName)
                    .ageGroup(ageCode)
                    .country(ctyCode)
                    .build());
        }

        // ---------- Persistencia ----------
        int insCountries, insThemes, insAges, insSets;
        EntityManager em = LocalEntityManagerProvider.em();

        try {
            em.getTransaction().begin();

            insCountries = persistMapValues(em, countries);
            insThemes = persistMapValues(em, themes);
            insAges = persistMapValues(em, ages);

            // Reemplazar con entidades persistentes ya existentes
            for (var entry : countries.entrySet()) {
                var code = entry.getKey();
                var existing = em.createQuery("SELECT c FROM Country c WHERE c.code = :code", Country.class)
                        .setParameter("code", code)
                        .getResultStream().findFirst().orElse(null);
                if (existing != null) countries.put(code, existing);
            }
            for (var entry : themes.entrySet()) {
                var name = entry.getKey();
                var existing = em.createQuery("SELECT t FROM Theme t WHERE t.name = :name", Theme.class)
                        .setParameter("name", name)
                        .getResultStream().findFirst().orElse(null);
                if (existing != null) themes.put(name, existing);
            }
            for (var entry : ages.entrySet()) {
                var code = entry.getKey();
                var existing = em.createQuery("SELECT a FROM AgeGroup a WHERE a.code = :code", AgeGroup.class)
                        .setParameter("code", code)
                        .getResultStream().findFirst().orElse(null);
                if (existing != null) ages.put(code, existing);
            }

            // Insertar los sets usando entidades gestionadas
            insSets = 0;
            for (var p : pendings) {
                var set = LegoSet.builder()
                        .prodId(p.getProdId())
                        .setName(p.getSetName())
                        .prodDesc(p.getProdDesc())
                        .reviewDifficulty(p.getReviewDifficulty())
                        .pieceCount(p.getPieceCount())
                        .starRating(p.getStarRating())
                        .listPrice(p.getListPrice())
                        .theme(themes.get(p.getTheme()))
                        .ageGroup(ages.get(p.getAgeGroup()))
                        .country(countries.get(p.getCountry()))
                        .build();
                em.persist(set);
                insSets++;
            }

            em.getTransaction().commit();

        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }

        return ImportResult.builder()
                .filasLeidas(rows.size())
                .filasValidas(validas)
                .countriesInsertados(insCountries)
                .themesInsertados(insThemes)
                .ageGroupsInsertados(insAges)
                .setsInsertados(insSets)
                .build();
    }

    // ---------- Helpers ----------
    private static <T> int persistMapValues(EntityManager em, Map<String, T> map) {
        int count = 0;
        for (var e : map.entrySet()) {
            var value = e.getValue();

            // Evitar duplicados por código o nombre
            if (value instanceof Country c) {
                var exists = em.createQuery("SELECT COUNT(c) FROM Country c WHERE c.code = :code", Long.class)
                        .setParameter("code", c.getCode())
                        .getSingleResult();
                if (exists > 0) continue;
            }
            if (value instanceof Theme t) {
                var exists = em.createQuery("SELECT COUNT(t) FROM Theme t WHERE t.name = :name", Long.class)
                        .setParameter("name", t.getName())
                        .getSingleResult();
                if (exists > 0) continue;
            }
            if (value instanceof AgeGroup a) {
                var exists = em.createQuery("SELECT COUNT(a) FROM AgeGroup a WHERE a.code = :code", Long.class)
                        .setParameter("code", a.getCode())
                        .getSingleResult();
                if (exists > 0) continue;
            }

            em.persist(value);
            count++;
        }
        em.flush();
        return count;
    }

    private static String trimOrNull(String s) {
        return (s == null) ? null : (s.trim().isEmpty() ? null : s.trim());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String nvl(String s, String d) {
        return isBlank(s) ? d : s;
    }

    // ---------- Modelo temporal ----------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class PendingSet {
        private Integer prodId;
        private String setName;
        private String prodDesc;
        private String reviewDifficulty;
        private Integer pieceCount;
        private Double starRating;
        private java.math.BigDecimal listPrice;
        private String theme;
        private String ageGroup;
        private String country;
    }
}
