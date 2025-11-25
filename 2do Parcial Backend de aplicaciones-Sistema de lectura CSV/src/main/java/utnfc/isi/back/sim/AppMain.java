package utnfc.isi.back.sim;

import utnfc.isi.back.sim.csv.CsvLoader;
import utnfc.isi.back.sim.repository.JpaLegoSetRepository;
import utnfc.isi.back.sim.service.ImportService;

public class AppMain {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Uso: mvn -q exec:java -Dexec.args=\"/path/lego_sets_data.csv\"");
            return;
        }

        var path = args[0];

        // 1️⃣ Leer CSV
        var rows = CsvLoader.read(path);

        // 2️⃣ Importar datos a la base
        var svc = new ImportService();
        var result = svc.importar(rows);

        // 3️⃣ Mostrar resumen de importación
        System.out.println();
        System.out.println("   RESULTADO DE LA IMPORTACIÓN");
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.printf("Total de filas procesadas: %d%n", result.getFilasLeidas());
        System.out.printf("Filas válidas importadas: %d%n", result.getFilasValidas());
        System.out.printf("Filas saltadas: %d (campos vacíos o países no encontrados)%n",
                result.getFilasLeidas() - result.getFilasValidas());
        System.out.println();
        System.out.printf("Cantidad de Sets que se insertaron en la base de datos: %d%n",
                result.getSetsInsertados());
        System.out.printf("Cantidad de Rangos de edad que se insertaron en la base de datos: %d%n",
                result.getAgeGroupsInsertados());
        System.out.printf("Cantidad de Temáticas que se insertaron en la base de datos: %d%n",
                result.getThemesInsertados());
        System.out.println("────────────────────────────────────────────────────────────");

        // 4️⃣ Ranking costo/valoración
        var repo = new JpaLegoSetRepository();
        var ranking = repo.rankingPaisesPorCostoValoracion();

        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println("📊  RANKING DE LOS 5 PAÍSES CON LA RELACIÓN COSTO/VALORACIÓN MÁS BAJA");
        System.out.println("════════════════════════════════════════════════════════════");

        int i = 1;
        for (var r : ranking) {
            System.out.printf("%d. %s - Relación: %.2f%n", i++, r[0], (Double) r[1]);
        }

        System.out.println("────────────────────────────────────────────────────────────");

        // 5️⃣ Sets filtrados: edad = 3, precio < 10, rating ≥ 4.8
        int edadBuscada = 3;
        double precioMax = 10.0;

        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println("🎯  SETS PARA 3 AÑOS (Precio < $10,00, Rating >= 4,8)");
        System.out.println("════════════════════════════════════════════════════════════");

        var sets = repo.findByEdadPrecioValoracion(edadBuscada, precioMax);

        if (sets.isEmpty()) {
            System.out.println("No se encontraron sets que cumplan las condiciones.");
        } else {
            System.out.printf("Se encontraron %d sets que cumplen las condiciones:%n", sets.size());
            sets.stream().limit(4).forEach(s -> System.out.printf(
                    "• %s - $%.2f (⭐ %.1f) - %s%n",
                    s.getSetName(),
                    s.getListPrice().doubleValue(),
                    s.getStarRating(),
                    s.getTheme().getName()
            ));
        }

        System.out.println("────────────────────────────────────────────────────────────");
    }
}
