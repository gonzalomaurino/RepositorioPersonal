package com.tpi.logistica.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tramos", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID de la ruta es obligatorio")
    @Column(name = "id_ruta")
    private Long idRuta;

    @Column(name = "patente_camion")
    private String patenteCamion;

    @Column(name = "origen_descripcion")
    private String origenDescripcion;

    @Column(name = "destino_descripcion")
    private String destinoDescripcion;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_inicio_estimada")
    private LocalDateTime fechaInicioEstimada;

    @Column(name = "fecha_fin_estimada")
    private LocalDateTime fechaFinEstimada;

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    @Column(name = "costo_estimado")
    private Double costoEstimado;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "km_reales")
    private Double kmReales;
}
