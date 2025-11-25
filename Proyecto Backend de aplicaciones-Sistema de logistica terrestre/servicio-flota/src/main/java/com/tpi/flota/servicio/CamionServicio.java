package com.tpi.flota.servicio;

import com.tpi.flota.modelo.Camion;
import com.tpi.flota.repositorio.CamionRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CamionServicio {

    private final CamionRepositorio repositorio;

    public CamionServicio(CamionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Camion> listar() {
        return repositorio.findAll();
    }

    public Optional<Camion> buscarPorPatente(String patente) {
        return repositorio.findById(patente);
    }

    public List<Camion> listarDisponibles() {
        return repositorio.findByDisponible(true);
    }

    public boolean puedeTransportar(String patente, Double pesoContenedor, Double volumenContenedor) {
        return buscarPorPatente(patente)
                .map(camion ->
                    camion.getCapacidadPeso() >= pesoContenedor &&
                    camion.getCapacidadVolumen() >= volumenContenedor
                )
                .orElse(false);
    }

    public List<Camion> encontrarCamionesAptos(Double pesoContenedor, Double volumenContenedor) {
        return repositorio.findByDisponible(true).stream()
                .filter(c -> c.getCapacidadPeso() >= pesoContenedor &&
                            c.getCapacidadVolumen() >= volumenContenedor)
                .toList();
    }

    public Camion guardar(Camion nuevoCamion) {
        if (repositorio.existsById(nuevoCamion.getPatente())) {
            throw new RuntimeException("Ya existe un camión con esa patente");
        }
        return repositorio.save(nuevoCamion);
    }

    public Camion actualizar(String patente, Camion datosActualizados) {
        return repositorio.findById(patente)
                .map(camion -> {
                    camion.setNombreTransportista(datosActualizados.getNombreTransportista());
                    camion.setTelefonoTransportista(datosActualizados.getTelefonoTransportista());
                    camion.setCapacidadPeso(datosActualizados.getCapacidadPeso());
                    camion.setCapacidadVolumen(datosActualizados.getCapacidadVolumen());
                    camion.setConsumoCombustibleKm(datosActualizados.getConsumoCombustibleKm());
                    camion.setCostoKm(datosActualizados.getCostoKm());
                    camion.setDisponible(datosActualizados.getDisponible());
                    return repositorio.save(camion);
                })
                .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
    }

    public Camion cambiarDisponibilidad(String patente, Boolean disponible) {
        return repositorio.findById(patente)
                .map(camion -> {
                    camion.setDisponible(disponible);
                    return repositorio.save(camion);
                })
                .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
    }

    public void eliminar(String patente) {
        if (!repositorio.existsById(patente)) {
            throw new RuntimeException("Camión no encontrado con patente: " + patente);
        }
        repositorio.deleteById(patente);
    }
}

