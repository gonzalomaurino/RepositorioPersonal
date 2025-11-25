package com.tpi.gestion.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "tarifas", schema = "gestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @PositiveOrZero(message = "El rango mínimo de peso no puede ser negativo")
    @Column(name = "rango_peso_min")
    private Double rangoPesoMin;

    @Positive(message = "El rango máximo de peso debe ser mayor a cero")
    @Column(name = "rango_peso_max")
    private Double rangoPesoMax;

    @PositiveOrZero(message = "El rango mínimo de volumen no puede ser negativo")
    @Column(name = "rango_volumen_min")
    private Double rangoVolumenMin;

    @Positive(message = "El rango máximo de volumen debe ser mayor a cero")
    @Column(name = "rango_volumen_max")
    private Double rangoVolumenMax;

    @Positive(message = "El valor de la tarifa debe ser positivo")
    private Double valor;
}
