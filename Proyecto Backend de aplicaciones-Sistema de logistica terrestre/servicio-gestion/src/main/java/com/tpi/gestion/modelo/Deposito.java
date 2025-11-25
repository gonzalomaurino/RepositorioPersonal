package com.tpi.gestion.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;


@Entity
@Table(name = "depositos", schema = "gestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del depósito es obligatorio")
    private String nombre;

    @NotBlank(message = "La dirección del depósito es obligatoria")
    private String direccion;

    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitud;

    @DecimalMin(value = "0.0", message = "El costo de estadía debe ser mayor o igual a 0")
    @Column(name = "costo_estadia_xdia")
    private Double costoEstadiaXdia;
}
