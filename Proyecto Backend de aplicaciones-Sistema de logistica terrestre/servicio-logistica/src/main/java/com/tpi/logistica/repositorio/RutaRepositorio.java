package com.tpi.logistica.repositorio;

import com.tpi.logistica.modelo.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaRepositorio extends JpaRepository<Ruta, Long> {

    List<Ruta> findByIdSolicitud(Long idSolicitud);
}

