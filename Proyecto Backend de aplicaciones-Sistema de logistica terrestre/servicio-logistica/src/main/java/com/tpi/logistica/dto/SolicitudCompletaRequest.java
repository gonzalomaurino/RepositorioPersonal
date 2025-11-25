package com.tpi.logistica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para crear una solicitud completa incluyendo datos del cliente y contenedor.
 * Si el cliente o contenedor no existen, se crearán automáticamente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCompletaRequest {

    // ========== DATOS DE LA SOLICITUD ==========
    @NotBlank(message = "El número de seguimiento es obligatorio")
    private String numeroSeguimiento;
    
    @NotBlank(message = "La dirección de origen es obligatoria")
    private String origenDireccion;
    
    @NotNull(message = "La latitud de origen es obligatoria")
    private Double origenLatitud;
    
    @NotNull(message = "La longitud de origen es obligatoria")
    private Double origenLongitud;
    
    @NotBlank(message = "La dirección de destino es obligatoria")
    private String destinoDireccion;
    
    @NotNull(message = "La latitud de destino es obligatoria")
    private Double destinoLatitud;
    
    @NotNull(message = "La longitud de destino es obligatoria")
    private Double destinoLongitud;

    // ========== DATOS DEL CLIENTE (Opcional si ya existe) ==========
    private Long idCliente; // Si existe, se usa; si no, se crea con los datos siguientes
    
    private String clienteNombre;
    private String clienteApellido;
    
    @Email(message = "El email del cliente debe ser válido")
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteCuil;

    // ========== DATOS DEL CONTENEDOR (Opcional si ya existe) ==========
    private Long idContenedor; // Si existe, se usa; si no, se crea con los datos siguientes
    
    @NotBlank(message = "El código de identificación del contenedor es obligatorio")
    @JsonProperty("codigoIdentificacion")  // Acepta tanto codigoIdentificacion como contenedorCodigo
    private String codigoIdentificacion;
    
    // Alias para compatibilidad con tests que usan contenedorCodigo
    @JsonProperty("contenedorCodigo")
    public void setContenedorCodigo(String codigo) {
        this.codigoIdentificacion = codigo;
    }
    
    @NotNull(message = "El peso del contenedor es obligatorio")
    private Double peso;
    
    @NotNull(message = "El volumen del contenedor es obligatorio")
    private Double volumen;
}
