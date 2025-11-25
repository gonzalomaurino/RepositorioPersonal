package com.tpi.gestion.repositorio;

import com.tpi.gestion.modelo.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepositorio extends JpaRepository<Tarifa, Long> {
}
