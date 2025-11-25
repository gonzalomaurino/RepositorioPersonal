package com.tpi.logistica.servicio;

import com.tpi.logistica.modelo.Configuracion;
import com.tpi.logistica.repositorio.ConfiguracionRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracionServicio {

    private final ConfiguracionRepositorio repositorio;

    public ConfiguracionServicio(ConfiguracionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Configuracion> listar() {
        return repositorio.findAll();
    }

    public Optional<Configuracion> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Optional<Configuracion> buscarPorClave(String clave) {
        return repositorio.findByClave(clave);
    }

    public Configuracion guardar(Configuracion nuevaConfiguracion) {
        if (repositorio.existsByClave(nuevaConfiguracion.getClave())) {
            throw new RuntimeException("Ya existe una configuración con esa clave");
        }
        return repositorio.save(nuevaConfiguracion);
    }

    public Configuracion actualizar(Long id, Configuracion datosActualizados) {
        return repositorio.findById(id)
                .map(config -> {
                    config.setClave(datosActualizados.getClave());
                    config.setValor(datosActualizados.getValor());
                    return repositorio.save(config);
                })
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada"));
    }

    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }
}
