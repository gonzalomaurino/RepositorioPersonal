package com.tpi.gestion.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudLogisticaDTO {
    
    private Long id;
    private String numeroSeguimiento;
    private Long idContenedor;
    private Long idCliente;
    private String estado;
    private Double costoEstimado;
    private Double costoFinal;
    private Double tiempoEstimado;
    private Double tiempoReal;
    

    private String ubicacionActual;
    private String descripcionUbicacion;
    

    private TramoActual tramoActual;
    
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
