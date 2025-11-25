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

import com.tpi.gestion.modelo.Cliente;
import com.tpi.gestion.servicio.ClienteServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para gestión de clientes del sistema de contenedores")
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
        summary = "Listar todos los clientes",
        description = "Obtiene una lista completa de todos los clientes registrados en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT inválido o ausente", content = @Content)
    })
    public List<Cliente> listarTodos() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
        summary = "Buscar cliente por ID",
        description = "Obtiene los detalles de un cliente específico mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Cliente> buscarPorId(
        @Parameter(description = "ID del cliente a buscar", required = true, example = "1")
        @PathVariable Long id
    ) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(
        summary = "Crear nuevo cliente",
        description = "Registra un nuevo cliente en el sistema con los datos proporcionados. Los operadores pueden crear cualquier cliente. Los clientes pueden auto-registrarse a través de la creación de solicitudes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos - Error de validación", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Cliente> crear(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del cliente a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = Cliente.class))
        )
        @Valid @RequestBody Cliente cliente
    ) {
        return ResponseEntity.ok(servicio.guardar(cliente));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
        summary = "Actualizar cliente existente",
        description = "Modifica los datos de un cliente existente identificado por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Cliente> actualizar(
        @Parameter(description = "ID del cliente a actualizar", required = true, example = "1")
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nuevos datos del cliente",
            required = true,
            content = @Content(schema = @Schema(implementation = Cliente.class))
        )
        @Valid @RequestBody Cliente datos
    ) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
        summary = "Eliminar cliente",
        description = "Elimina un cliente del sistema mediante su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Void> eliminar(
        @Parameter(description = "ID del cliente a eliminar", required = true, example = "1")
        @PathVariable Long id
    ) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
