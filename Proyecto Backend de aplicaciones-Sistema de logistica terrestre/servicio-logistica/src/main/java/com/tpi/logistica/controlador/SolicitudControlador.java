package com.tpi.logistica.controlador;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.logistica.dto.ContenedorPendienteResponse;
import com.tpi.logistica.dto.EstimacionRutaRequest;
import com.tpi.logistica.dto.EstimacionRutaResponse;
import com.tpi.logistica.dto.SeguimientoSolicitudResponse;
import com.tpi.logistica.dto.SolicitudCompletaRequest;
import com.tpi.logistica.dto.SolicitudCompletaResponse;
import com.tpi.logistica.modelo.Solicitud;
import com.tpi.logistica.servicio.SolicitudServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/solicitudes")
@Tag(name = "Solicitudes", description = "API para gestión de solicitudes de transporte de contenedores")
@SecurityRequirement(name = "Bearer Authentication")
public class SolicitudControlador {

    private final SolicitudServicio servicio;

    public SolicitudControlador(SolicitudServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Solicitud> listarTodas() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<Solicitud> buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/seguimiento/{numeroSeguimiento}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<Solicitud> buscarPorNumeroSeguimiento(@PathVariable String numeroSeguimiento) {
        return servicio.buscarPorNumeroSeguimiento(numeroSeguimiento)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public List<Solicitud> listarPorCliente(@PathVariable Long idCliente) {
        return servicio.listarPorCliente(idCliente);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Solicitud> listarPorEstado(@PathVariable String estado) {
        return servicio.listarPorEstado(estado);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<Solicitud> crear(@Valid @RequestBody Solicitud solicitud) {
        Solicitud nueva = servicio.guardar(solicitud);
        return ResponseEntity.ok(nueva);
    }

    /**
     * Crea una solicitud completa incluyendo cliente y contenedor si no existen.
     * Este endpoint implementa el requerimiento de crear la solicitud junto con el contenedor.
     * 
     * Casos de uso:
     * 1. Cliente nuevo + Contenedor nuevo: Se crean ambos automáticamente
     * 2. Cliente existente + Contenedor nuevo: Se usa el cliente existente y se crea el contenedor
     * 3. Cliente existente + Contenedor existente: Se usan ambos existentes
     * 
     * @param request Datos completos de la solicitud, cliente y contenedor
     * @return Response con IDs generados e información de qué se creó
     */
    @PostMapping("/completa")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<SolicitudCompletaResponse> crearSolicitudCompleta(
            @Valid @RequestBody SolicitudCompletaRequest request) {
        SolicitudCompletaResponse response = servicio.crearSolicitudCompleta(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Solicitud> actualizar(@PathVariable Long id,
                                               @Valid @RequestBody Solicitud datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/estimar-ruta")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<EstimacionRutaResponse> estimarRuta(@Valid @RequestBody EstimacionRutaRequest request) {
        EstimacionRutaResponse estimacion = servicio.estimarRuta(request);
        return ResponseEntity.ok(estimacion);
    }

    @PostMapping("/{id}/asignar-ruta")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Solicitud> asignarRuta(@PathVariable Long id,
                                                 @Valid @RequestBody EstimacionRutaRequest datosRuta) {
        Solicitud solicitud = servicio.asignarRuta(id, datosRuta);
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping("/seguimiento-detallado/{numeroSeguimiento}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<SeguimientoSolicitudResponse> obtenerSeguimientoDetallado(
            @PathVariable String numeroSeguimiento) {
        SeguimientoSolicitudResponse seguimiento = servicio.obtenerSeguimiento(numeroSeguimiento);
        return ResponseEntity.ok(seguimiento);
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<ContenedorPendienteResponse>> listarPendientes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idContenedor) {
        List<ContenedorPendienteResponse> pendientes = servicio.listarPendientes(estado, idContenedor);
        return ResponseEntity.ok(pendientes);
    }
}
