package com.tpi.flota.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.flota.modelo.Camion;
import com.tpi.flota.servicio.CamionServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/camiones")
public class CamionControlador {

    private final CamionServicio servicio;

    public CamionControlador(CamionServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Camion> listarTodos() {
        return servicio.listar();
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Camion> listarDisponibles() {
        return servicio.listarDisponibles();
    }

    @GetMapping("/{patente}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('TRANSPORTISTA')")
    public ResponseEntity<Camion> buscarPorPatente(@PathVariable String patente) {
        return servicio.buscarPorPatente(patente)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aptos")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Camion> buscarCamionesAptos(@RequestParam Double peso, @RequestParam Double volumen) {
        return servicio.encontrarCamionesAptos(peso, volumen);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Camion> crear(@Valid @RequestBody Camion camion) {
        return ResponseEntity.ok(servicio.guardar(camion));
    }

    @PutMapping("/{patente}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Camion> actualizar(@PathVariable String patente,
                                             @Valid @RequestBody Camion datos) {
        return ResponseEntity.ok(servicio.actualizar(patente, datos));
    }

    @PatchMapping("/{patente}/disponibilidad")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('TRANSPORTISTA')")
    public ResponseEntity<Camion> cambiarDisponibilidad(@PathVariable String patente,
                                                        @RequestParam Boolean disponible) {
        return ResponseEntity.ok(servicio.cambiarDisponibilidad(patente, disponible));
    }

    @DeleteMapping("/{patente}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable String patente) {
        servicio.eliminar(patente);
        return ResponseEntity.noContent().build();
    }
}

