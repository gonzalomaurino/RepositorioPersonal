// repository/LegoSetRepository.java
package utnfc.isi.back.sim.repository;
import utnfc.isi.back.sim.domain.*;
import java.util.List;
import java.util.Optional;

public interface LegoSetRepository extends CrudRepository<LegoSet, Integer> {
  Optional<LegoSet> findByProdId(Integer prodId);
  List<LegoSet> findByTheme(Theme t);
  List<LegoSet> findByCountry(Country c);
  List<LegoSet> findByAgeGroup(AgeGroup a);
  List<LegoSet> findAllWithRefs();

  List<Object[]> top5ThemesPorPiezas(); // (themeName, sumPieces)
  List<Object[]> countriesConMasDeN(int n); // (countryCode, countSets)
  List<Object[]> rankingThemesPorRatingPromedio(int minPieces); // (themeName, avgRating)
}