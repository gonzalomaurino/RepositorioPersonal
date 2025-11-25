package com.tpi.logistica.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimacionRutaRequest {
    private Long idContenedor;
    private Long idCliente;
    private String origenDireccion;
    private Double origenLatitud;
    private Double origenLongitud;
    private String destinoDireccion;
    private Double destinoLatitud;
    private Double destinoLongitud;
    private Double pesoKg;
    private Double volumenM3;
}

