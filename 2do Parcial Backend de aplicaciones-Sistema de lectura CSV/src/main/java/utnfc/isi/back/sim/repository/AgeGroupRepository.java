package utnfc.isi.back.sim.repository;
import utnfc.isi.back.sim.domain.AgeGroup;
import java.util.Optional;
public interface AgeGroupRepository extends CrudRepository<AgeGroup, Integer> {
  Optional<AgeGroup> findByCode(String code);
}