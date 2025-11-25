package com.tpi.gestion.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoContenedorResponse {


    private Long idContenedor;
    private String codigoIdentificacion;
    private Double peso;
    private Double volumen;
    

    private ClienteInfo cliente;
    

    private SolicitudInfo solicitud;
    

    private String ubicacionActual;
    private String descripcionUbicacion;
    

    private TramoInfo tramoActual;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteInfo {
        private Long id;
        private String nombre;
        private String apellido;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolicitudInfo {
        private Long id;
        private String numeroSeguimiento;
        private String estado;
        private Double costoEstimado;
        private Double costoFinal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TramoInfo {
        private String origen;
        private String destino;
        private String estadoTramo;
        private String patenteCamion;
    }
}
