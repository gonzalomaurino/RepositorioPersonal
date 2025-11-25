package com.tpi.gestion.repositorio;

import com.tpi.gestion.modelo.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositoRepositorio extends JpaRepository<Deposito, Long> {
}
