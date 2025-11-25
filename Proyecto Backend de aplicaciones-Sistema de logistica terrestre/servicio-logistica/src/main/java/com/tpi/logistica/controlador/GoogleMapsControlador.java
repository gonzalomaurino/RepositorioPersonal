package com.tpi.logistica.controlador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.logistica.dto.googlemaps.DistanciaYDuracion;
import com.tpi.logistica.servicio.GoogleMapsService;

@RestController
@RequestMapping("/google-maps")
public class GoogleMapsControlador {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsControlador.class);

    private final GoogleMapsService googleMapsService;

    public GoogleMapsControlador(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @GetMapping("/distancia")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<?> calcularDistancia(
            @RequestParam(required = true) String origen,
            @RequestParam(required = true) String destino) {

        logger.info("Request recibido: calcular distancia de {} a {}", origen, destino);


        if (origen == null || origen.isBlank() || destino == null || destino.isBlank()) {
            logger.warn("Parámetros inválidos: origen={}, destino={}", origen, destino);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Parámetros origen y destino son requeridos"));
        }

        try {

            DistanciaYDuracion resultado = googleMapsService
                    .calcularDistanciaYDuracion(origen, destino);

            logger.info("Cálculo exitoso: {}km en {}h",
                resultado.getDistanciaKm(),
                resultado.getDuracionHoras());

            return ResponseEntity.ok(resultado);

        } catch (RuntimeException e) {

            logger.error("Error al calcular distancia", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al calcular distancia: " + e.getMessage()));
        }
    }

    @GetMapping("/distancia-coords")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<?> calcularDistanciaPorCoordenadas(
            @RequestParam(required = true) Double lat1,
            @RequestParam(required = true) Double lng1,
            @RequestParam(required = true) Double lat2,
            @RequestParam(required = true) Double lng2) {

        logger.info("Request recibido: calcular distancia de ({},{}) a ({},{})",
                lat1, lng1, lat2, lng2);


        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            logger.warn("Parámetros de coordenadas inválidos");
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Todos los parámetros de coordenadas son requeridos"));
        }

        try {

            DistanciaYDuracion resultado = googleMapsService
                    .calcularDistanciaPorCoordenadas(lat1, lng1, lat2, lng2);

            logger.info("Cálculo exitoso por coordenadas: {}km", resultado.getDistanciaKm());

            return ResponseEntity.ok(resultado);

        } catch (RuntimeException e) {
            logger.error("Error al calcular distancia por coordenadas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al calcular distancia: " + e.getMessage()));
        }
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }


        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}

