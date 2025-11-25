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

import com.tpi.gestion.modelo.Deposito;
import com.tpi.gestion.servicio.DepositoServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/depositos")
public class DepositoControlador {

    private final DepositoServicio servicio;


    public DepositoControlador(DepositoServicio servicio) {
        this.servicio = servicio;
    }


    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Deposito> listarTodos() {
        return servicio.listar();
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Deposito> buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Deposito> crear(@Valid @RequestBody Deposito deposito) {
        Deposito nuevo = servicio.guardar(deposito);
        return ResponseEntity.ok(nuevo);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Deposito> actualizar(@PathVariable Long id,
                                               @Valid @RequestBody Deposito datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
