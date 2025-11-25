// repository/JpaThemeRepository.java
package utnfc.isi.back.sim.repository;

import jakarta.persistence.TypedQuery;
import utnfc.isi.back.sim.domain.Theme;
import java.util.List;
import java.util.Optional;

public class JpaThemeRepository extends JpaRepositoryBase<Theme, Integer> implements ThemeRepository {
  public JpaThemeRepository(){ super(Theme.class); }

  @Override public Optional<Theme> findByName(String name){
    var em = em();
    try {
      TypedQuery<Theme> q = em.createQuery("select t from Theme t where t.name = :n", Theme.class);
      q.setParameter("n", name);
      var list = q.getResultList();
      return list.isEmpty()? Optional.empty(): Optional.of(list.get(0));
    } finally { em.close(); }
  }

  @Override public List<Theme> findAllOrderByName(){
    var em = em();
    try { return em.createQuery("select t from Theme t order by t.name", Theme.class).getResultList(); }
    finally { em.close(); }
  }
}
