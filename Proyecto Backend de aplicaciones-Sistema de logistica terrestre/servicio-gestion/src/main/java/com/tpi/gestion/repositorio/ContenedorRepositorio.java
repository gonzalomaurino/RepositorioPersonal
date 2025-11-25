package com.tpi.gestion.repositorio;

import com.tpi.gestion.modelo.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenedorRepositorio extends JpaRepository<Contenedor, Long> {


    List<Contenedor> findByClienteId(Long idCliente);


    boolean existsByCodigoIdentificacion(String codigoIdentificacion);


    java.util.Optional<Contenedor> findByCodigoIdentificacion(String codigoIdentificacion);
}
