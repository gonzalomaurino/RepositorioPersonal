package com.tpi.gestion.servicio;

import com.tpi.gestion.modelo.Tarifa;
import com.tpi.gestion.repositorio.TarifaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaServicio {

    private final TarifaRepositorio repositorio;

    public TarifaServicio(TarifaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Tarifa> listar() {
        return repositorio.findAll();
    }

    public Optional<Tarifa> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Tarifa guardar(Tarifa nueva) {
        return repositorio.save(nueva);
    }

    public Tarifa actualizar(Long id, Tarifa datos) {
        return repositorio.findById(id)
                .map(t -> {
                    t.setDescripcion(datos.getDescripcion());
                    t.setRangoPesoMin(datos.getRangoPesoMin());
                    t.setRangoPesoMax(datos.getRangoPesoMax());
                    t.setRangoVolumenMin(datos.getRangoVolumenMin());
                    t.setRangoVolumenMax(datos.getRangoVolumenMax());
                    t.setValor(datos.getValor());
                    return repositorio.save(t);
                })
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con ID: " + id));
    }

    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RuntimeException("Tarifa no encontrada con ID: " + id);
        }
        repositorio.deleteById(id);
    }

    public Optional<Tarifa> buscarTarifaAplicable(Double peso, Double volumen) {
        return repositorio.findAll().stream()
                .filter(t ->
                    peso >= t.getRangoPesoMin() && peso <= t.getRangoPesoMax() &&
                    volumen >= t.getRangoVolumenMin() && volumen <= t.getRangoVolumenMax()
                )
                .findFirst();
    }
}
