package com.tpi.logistica.dto;

import lombok.*;

/**
 * DTO de respuesta para una solicitud completa creada.
 * Incluye los IDs generados del cliente y contenedor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCompletaResponse {
    
    private Long idSolicitud;
    private String numeroSeguimiento;
    private String estado;
    
    private Long idCliente;
    private boolean clienteCreado; // true si se creó automáticamente
    
    private Long idContenedor;
    private String codigoIdentificacion;
    private boolean contenedorCreado; // true si se creó automáticamente
    
    private String origenDireccion;
    private String destinoDireccion;
    
    private String mensaje;
}
