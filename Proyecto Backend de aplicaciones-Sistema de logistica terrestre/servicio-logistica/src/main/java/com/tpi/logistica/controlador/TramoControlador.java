package com.tpi.logistica.controlador;

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

import com.tpi.logistica.modelo.Tramo;
import com.tpi.logistica.servicio.TramoServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tramos")
public class TramoControlador {

    private final TramoServicio servicio;

    public TramoControlador(TramoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Tramo> listarTodos() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('TRANSPORTISTA')")
    public ResponseEntity<Tramo> buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ruta/{idRuta}")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Tramo> listarPorRuta(@PathVariable Long idRuta) {
        return servicio.listarPorRuta(idRuta);
    }

    @GetMapping("/camion/{patenteCamion}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('TRANSPORTISTA')")
    public List<Tramo> listarPorCamion(@PathVariable String patenteCamion) {
        return servicio.listarPorCamion(patenteCamion);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Tramo> listarPorEstado(@PathVariable String estado) {
        return servicio.listarPorEstado(estado);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Tramo> crear(@Valid @RequestBody Tramo tramo) {
        return ResponseEntity.ok(servicio.guardar(tramo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Tramo> actualizar(@PathVariable Long id,
                                           @Valid @RequestBody Tramo datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/asignar-camion")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Tramo> asignarCamion(@PathVariable Long id,
                                               @RequestParam String patente,
                                               @RequestParam Double peso,
                                               @RequestParam Double volumen) {
        Tramo tramo = servicio.asignarCamion(id, patente, peso, volumen);
        return ResponseEntity.ok(tramo);
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('TRANSPORTISTA')")
    public ResponseEntity<Tramo> iniciarTramo(@PathVariable Long id) {
        Tramo tramo = servicio.iniciarTramo(id);
        return ResponseEntity.ok(tramo);
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('TRANSPORTISTA')")
    public ResponseEntity<Tramo> finalizarTramo(@PathVariable Long id,
                                               @RequestParam Double kmReales,
                                               @RequestParam Double costoKmCamion,
                                               @RequestParam Double consumoCamion) {
        Tramo tramo = servicio.finalizarTramo(id, kmReales, costoKmCamion, consumoCamion);
        return ResponseEntity.ok(tramo);
    }
}
