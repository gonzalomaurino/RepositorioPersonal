package com.tpi.logistica.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorPendienteResponse {

    private Long idSolicitud;
    private String numeroSeguimiento;
    private Long idContenedor;
    private Long idCliente;
    

    private String estado;
    

    private String ubicacionActual;
    private String descripcionUbicacion;
    

    private TramoActual tramoActual;
    

    private Double costoEstimado;
    private Double costoFinal;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TramoActual {
        private Long idTramo;
        private String origen;
        private String destino;
        private String estadoTramo;
        private String patenteCamion;
    }
}
