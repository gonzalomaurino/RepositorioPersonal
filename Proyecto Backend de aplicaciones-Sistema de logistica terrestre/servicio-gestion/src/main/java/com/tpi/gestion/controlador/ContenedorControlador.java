package com.tpi.gestion.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.gestion.dto.EstadoContenedorResponse;
import com.tpi.gestion.modelo.Contenedor;
import com.tpi.gestion.servicio.ContenedorServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/contenedores")
public class ContenedorControlador {

    private final ContenedorServicio servicio;

    public ContenedorControlador(ContenedorServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Contenedor> listarTodos() {
        return servicio.listar();
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public List<Contenedor> listarPorCliente(@PathVariable Long idCliente) {
        return servicio.listarPorCliente(idCliente);
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<Contenedor> buscarPorCodigo(@PathVariable String codigo) {
        return servicio.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}/estado")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<EstadoContenedorResponse> obtenerEstadoPorCodigo(@PathVariable String codigo) {
        EstadoContenedorResponse estado = servicio.obtenerEstadoPorCodigo(codigo);
        return ResponseEntity.ok(estado);
    }


    @GetMapping("/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<EstadoContenedorResponse> obtenerEstado(@PathVariable Long id) {
        EstadoContenedorResponse estado = servicio.obtenerEstado(id);
        return ResponseEntity.ok(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<Contenedor> buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    public ResponseEntity<Contenedor> crear(@Valid @RequestBody Contenedor contenedor) {
        return ResponseEntity.ok(servicio.guardar(contenedor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Contenedor> actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody Contenedor datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
