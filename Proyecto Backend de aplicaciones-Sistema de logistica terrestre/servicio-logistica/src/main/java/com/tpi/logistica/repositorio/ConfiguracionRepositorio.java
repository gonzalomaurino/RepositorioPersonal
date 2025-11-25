package com.tpi.logistica.repositorio;

import com.tpi.logistica.modelo.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionRepositorio extends JpaRepository<Configuracion, Long> {

    Optional<Configuracion> findByClave(String clave);

    boolean existsByClave(String clave);
}

