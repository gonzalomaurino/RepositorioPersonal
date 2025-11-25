package com.tpi.logistica.servicio;

import com.tpi.logistica.modelo.Ruta;
import com.tpi.logistica.repositorio.RutaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RutaServicio {

    private final RutaRepositorio repositorio;

    public RutaServicio(RutaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Ruta> listar() {
        return repositorio.findAll();
    }

    public Optional<Ruta> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public List<Ruta> listarPorSolicitud(Long idSolicitud) {
        return repositorio.findByIdSolicitud(idSolicitud);
    }

    public Ruta guardar(Ruta nuevaRuta) {
        return repositorio.save(nuevaRuta);
    }

    public Ruta actualizar(Long id, Ruta datosActualizados) {
        return repositorio.findById(id)
                .map(ruta -> {
                    ruta.setIdSolicitud(datosActualizados.getIdSolicitud());
                    return repositorio.save(ruta);
                })
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con ID: " + id));
    }

    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RuntimeException("Ruta no encontrada con ID: " + id);
        }
        repositorio.deleteById(id);
    }
}

