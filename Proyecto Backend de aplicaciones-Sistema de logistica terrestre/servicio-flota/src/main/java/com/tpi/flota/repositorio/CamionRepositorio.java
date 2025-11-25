package com.tpi.flota.repositorio;

import com.tpi.flota.modelo.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CamionRepositorio extends JpaRepository<Camion, String> {

    List<Camion> findByDisponible(Boolean disponible);
}

