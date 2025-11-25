package com.tpi.logistica.repositorio;

import com.tpi.logistica.modelo.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepositorio extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByNumeroSeguimiento(String numeroSeguimiento);

    List<Solicitud> findByIdCliente(Long idCliente);

    List<Solicitud> findByIdContenedor(Long idContenedor);

    List<Solicitud> findByEstado(String estado);

    boolean existsByNumeroSeguimiento(String numeroSeguimiento);
}

