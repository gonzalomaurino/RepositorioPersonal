package com.tpi.logistica.controlador;

import com.tpi.logistica.modelo.Configuracion;
import com.tpi.logistica.servicio.ConfiguracionServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/configuraciones")
public class ConfiguracionControlador {

    private final ConfiguracionServicio servicio;

    public ConfiguracionControlador(ConfiguracionServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Configuracion> listarTodas() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Configuracion> buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clave/{clave}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Configuracion> buscarPorClave(@PathVariable String clave) {
        return servicio.buscarPorClave(clave)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Configuracion> crear(@Valid @RequestBody Configuracion configuracion) {
        Configuracion nueva = servicio.guardar(configuracion);
        return ResponseEntity.ok(nueva);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Configuracion> actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody Configuracion datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
