// repository/JpaAgeGroupRepository.java
package utnfc.isi.back.sim.repository;

import jakarta.persistence.TypedQuery;
import utnfc.isi.back.sim.domain.AgeGroup;
import java.util.Optional;

public class JpaAgeGroupRepository extends JpaRepositoryBase<AgeGroup, Integer> implements AgeGroupRepository {
  public JpaAgeGroupRepository(){ super(AgeGroup.class); }

  @Override public Optional<AgeGroup> findByCode(String code){
    var em = em();
    try {
      TypedQuery<AgeGroup> q = em.createQuery("select a from AgeGroup a where a.code = :c", AgeGroup.class);
      q.setParameter("c", code);
      var list = q.getResultList();
      return list.isEmpty()? Optional.empty(): Optional.of(list.get(0));
    } finally { em.close(); }
  }
}
