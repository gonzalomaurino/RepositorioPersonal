package com.tpi.logistica.servicio;

import com.tpi.logistica.dto.googlemaps.GoogleMapsDistanceResponse;
import com.tpi.logistica.dto.googlemaps.DistanciaYDuracion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@SuppressWarnings("deprecation")
public class GoogleMapsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);
    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public GoogleMapsService(RestClient restClient) {
        this.restClient = restClient;
    }


    public DistanciaYDuracion calcularDistanciaYDuracion(String origen, String destino) {
        try {

            String url = UriComponentsBuilder.fromHttpUrl(DISTANCE_MATRIX_URL)
                    .queryParam("origins", origen)
                    .queryParam("destinations", destino)
                    .queryParam("key", apiKey)
                    .queryParam("language", "es")
                    .toUriString();

            logger.info("Llamando a Google Maps API: origen={}, destino={}", origen, destino);
            logger.info("URL completa (con API key censurada): {}", url.replaceAll("key=[^&]+", "key=***"));



            GoogleMapsDistanceResponse response = restClient.get()
                    .uri(url)
                    .retrieve()

                    .onStatus(status -> !status.is2xxSuccessful(),
                        (request, response_) -> {
                            logger.error("Error HTTP {} al llamar Google Maps: {}",
                                response_.getStatusCode(), response_.getStatusText());
                            throw new RuntimeException("Error HTTP " + response_.getStatusCode() +
                                " en Google Maps API");
                        })

                    .body(GoogleMapsDistanceResponse.class);


            if (response == null || !"OK".equals(response.getStatus())) {
                logger.error("Error en respuesta de Google Maps: status={}, response completa={}",
                    response != null ? response.getStatus() : "null", response);
                throw new RuntimeException("Error al consultar Google Maps API: " +
                    (response != null ? response.getStatus() : "respuesta nula"));
            }

            if (response.getRows().isEmpty() || response.getRows().get(0).getElements().isEmpty()) {
                logger.warn("No se encontraron rutas entre: {} y {}", origen, destino);
                throw new RuntimeException("No se encontraron rutas entre origen y destino");
            }


            GoogleMapsDistanceResponse.Element element = response.getRows().get(0).getElements().get(0);


            if (!"OK".equals(element.getStatus())) {
                logger.error("Estado de elemento no válido: {}. Element completo: {}", element.getStatus(), element);
                throw new RuntimeException("No se pudo calcular la ruta: " + element.getStatus());
            }


            Double distanciaKm = element.getDistance().getValue() / 1000.0;
            Double duracionHoras = element.getDuration().getValue() / 3600.0;

            logger.info("Resultado exitoso: distancia={}km, duración={}h",
                distanciaKm, duracionHoras);


            return DistanciaYDuracion.builder()
                    .distanciaKm(distanciaKm)
                    .distanciaTexto(element.getDistance().getText())
                    .duracionHoras(duracionHoras)
                    .duracionTexto(element.getDuration().getText())
                    .origenDireccion(response.getOriginAddresses().get(0))
                    .destinoDireccion(response.getDestinationAddresses().get(0))
                    .build();

        } catch (RuntimeException e) {

            logger.error("Excepción controlada al calcular distancia", e);
            throw e;
        } catch (Exception e) {

            logger.error("Error inesperado al llamar a Google Maps API", e);
            throw new RuntimeException("Error al calcular distancia: " + e.getMessage(), e);
        }
    }


    public DistanciaYDuracion calcularDistanciaPorCoordenadas(
            Double origenLat, Double origenLng,
            Double destinoLat, Double destinoLng) {


        String origen = String.format("%f,%f", origenLat, origenLng);
        String destino = String.format("%f,%f", destinoLat, destinoLng);

        logger.info("Calculando distancia por coordenadas: ({},{}) → ({},{})",
            origenLat, origenLng, destinoLat, destinoLng);

        return calcularDistanciaYDuracion(origen, destino);
    }
}

