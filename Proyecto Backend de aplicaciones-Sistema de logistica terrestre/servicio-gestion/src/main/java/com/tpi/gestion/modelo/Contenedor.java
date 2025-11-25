package com.tpi.gestion.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "contenedores", schema = "gestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de identificación es obligatorio")
    @Column(name = "codigo_identificacion", unique = true, nullable = false)
    private String codigoIdentificacion;

    @DecimalMin(value = "0.1", message = "El peso del contenedor debe ser mayor a 0")
    @Column(name = "peso")
    private Double peso;

    @DecimalMin(value = "0.1", message = "El volumen del contenedor debe ser mayor a 0")
    @Column(name = "volumen")
    private Double volumen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @NotNull(message = "El contenedor debe pertenecer a un cliente")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private Cliente cliente;
}
