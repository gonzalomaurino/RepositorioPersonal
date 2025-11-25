// repository/ThemeRepository.java
package utnfc.isi.back.sim.repository;
import utnfc.isi.back.sim.domain.Theme;
import java.util.Optional;
import java.util.List;
public interface ThemeRepository extends CrudRepository<Theme, Integer> {
  Optional<Theme> findByName(String name);
  List<Theme> findAllOrderByName();
}