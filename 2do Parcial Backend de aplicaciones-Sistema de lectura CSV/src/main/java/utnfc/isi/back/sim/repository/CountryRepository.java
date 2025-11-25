// repository/CountryRepository.java
package utnfc.isi.back.sim.repository;
import utnfc.isi.back.sim.domain.Country;
import java.util.Optional;
public interface CountryRepository extends CrudRepository<Country, Integer> {
  Optional<Country> findByCode(String code);
}