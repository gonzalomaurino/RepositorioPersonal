package com.tpi.logistica.servicio;

import com.tpi.logistica.config.MicroserviciosConfig;
import com.tpi.logistica.dto.DepositoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepositoServicio {

    private static final Logger log = LoggerFactory.getLogger(DepositoServicio.class);
    
    // Constantes de configuración
    private static final Double DISTANCIA_MINIMA_DIVISION = 700.0; // km - No dividir si es menor
    private static final Double DISTANCIA_POR_TRAMO = 700.0; // km - Distancia máxima por tramo
    private static final Double PORCENTAJE_DESVIACION_MAX = 10.0; // % - Máxima desviación permitida de la ruta directa

    private final RestTemplate restTemplate;
    private final MicroserviciosConfig microserviciosConfig;

    public DepositoServicio(RestTemplate restTemplate, MicroserviciosConfig microserviciosConfig) {
        this.restTemplate = restTemplate;
        this.microserviciosConfig = microserviciosConfig;
    }

    /**
     * Busca depósitos intermedios estratégicamente ubicados en la ruta.
     * 
     * REGLAS DE DIVISIÓN:
     * - Distancia < 700 km → Tramo directo (sin depósitos)
     * - Distancia 700-1400 km → 1 depósito intermedio (2 tramos)
     * - Distancia 1400-2100 km → 2 depósitos intermedios (3 tramos)
     * - Y así sucesivamente...
     */
    public List<DepositoDTO> buscarDepositosEnRuta(
            Double origenLat, Double origenLng,
            Double destinoLat, Double destinoLng) {
        
        log.info("Buscando depósitos intermedios entre ({},{}) y ({},{})",
                origenLat, origenLng, destinoLat, destinoLng);

        // Calcular distancia directa origen-destino
        double distanciaDirecta = calcularDistancia(origenLat, origenLng, destinoLat, destinoLng);
        
        log.info("Distancia directa: {} km", distanciaDirecta);

        // 🔧 REGLA 1: Si la distancia es menor a 700 km, no usar depósitos
        if (distanciaDirecta < DISTANCIA_MINIMA_DIVISION) {
            log.info("Distancia menor a 700 km, usando tramo directo sin depósitos");
            return List.of();
        }

        // 🔧 REGLA 2: Calcular cuántos depósitos necesitamos
        int cantidadDepositosNecesarios = (int) Math.floor(distanciaDirecta / DISTANCIA_POR_TRAMO);
        
        log.info("Se necesitan {} depósitos intermedios para una distancia de {} km", 
                cantidadDepositosNecesarios, distanciaDirecta);

        try {
            // Obtener todos los depósitos disponibles
            String url = microserviciosConfig.getServicioGestionUrl() + "/depositos";
            
            ResponseEntity<List<DepositoDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<DepositoDTO>>() {}
            );

            List<DepositoDTO> todosLosDepositos = response.getBody();
            
            if (todosLosDepositos == null || todosLosDepositos.isEmpty()) {
                log.warn("No se encontraron depósitos en el sistema");
                return List.of();
            }

            // 🔧 REGLA 3: Buscar depósitos candidatos que estén "en el camino"
            List<DepositoCandidato> candidatos = new ArrayList<>();
            
            for (DepositoDTO deposito : todosLosDepositos) {
                if (deposito.getLatitud() == null || deposito.getLongitud() == null) {
                    continue;
                }

                double distanciaDesdeOrigen = calcularDistancia(
                    origenLat, origenLng,
                    deposito.getLatitud(), deposito.getLongitud()
                );

                double distanciaHaciaDestino = calcularDistancia(
                    deposito.getLatitud(), deposito.getLongitud(),
                    destinoLat, destinoLng
                );

                // Calcular desviación de la ruta directa
                double distanciaTotal = distanciaDesdeOrigen + distanciaHaciaDestino;
                double desviacion = distanciaTotal - distanciaDirecta;
                double porcentajeDesviacion = (desviacion / distanciaDirecta) * 100;

                // Solo considerar depósitos con desviación < 10%
                if (porcentajeDesviacion <= PORCENTAJE_DESVIACION_MAX) {
                    candidatos.add(new DepositoCandidato(
                        deposito,
                        distanciaDesdeOrigen,
                        distanciaHaciaDestino,
                        porcentajeDesviacion
                    ));
                    
                    log.debug("Depósito candidato: {} - Distancia desde origen: {} km, Desviación: {}%",
                            deposito.getNombre(), distanciaDesdeOrigen, porcentajeDesviacion);
                }
            }

            if (candidatos.isEmpty()) {
                log.warn("No se encontraron depósitos candidatos en la ruta");
                return List.of();
            }

            log.info("Total de depósitos candidatos: {}", candidatos.size());

            // Ordenar candidatos por distancia desde el origen
            candidatos.sort((c1, c2) -> Double.compare(c1.distanciaDesdeOrigen, c2.distanciaDesdeOrigen));

            // 🔧 REGLA 4: Seleccionar depósitos distribuidos uniformemente
            List<DepositoDTO> depositosSeleccionados = seleccionarDepositosEstrategicos(
                candidatos, 
                cantidadDepositosNecesarios, 
                distanciaDirecta
            );

            log.info("Depósitos seleccionados: {}", depositosSeleccionados.size());
            for (DepositoDTO deposito : depositosSeleccionados) {
                log.info("  - {} ({}, {})", deposito.getNombre(), 
                        deposito.getLatitud(), deposito.getLongitud());
            }

            return depositosSeleccionados;

        } catch (Exception e) {
            log.error("Error al buscar depósitos en ruta: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Selecciona los depósitos más estratégicos distribuidos uniformemente
     * a lo largo de la ruta.
     */
    private List<DepositoDTO> seleccionarDepositosEstrategicos(
            List<DepositoCandidato> candidatos,
            int cantidadNecesaria,
            double distanciaTotal) {

        List<DepositoDTO> seleccionados = new ArrayList<>();

        if (cantidadNecesaria == 0 || candidatos.isEmpty()) {
            return seleccionados;
        }

        if (cantidadNecesaria == 1) {
            // Para 1 solo depósito, buscar el más cercano a la mitad del recorrido
            double distanciaMitad = distanciaTotal / 2;
            DepositoCandidato mejorCandidato = null;
            double menorDiferencia = Double.MAX_VALUE;

            for (DepositoCandidato candidato : candidatos) {
                double diferencia = Math.abs(candidato.distanciaDesdeOrigen - distanciaMitad);
                if (diferencia < menorDiferencia) {
                    menorDiferencia = diferencia;
                    mejorCandidato = candidato;
                }
            }

            if (mejorCandidato != null) {
                seleccionados.add(mejorCandidato.deposito);
            }

        } else {
            // Para múltiples depósitos, distribuir uniformemente
            double intervalo = distanciaTotal / (cantidadNecesaria + 1);

            for (int i = 1; i <= cantidadNecesaria; i++) {
                double distanciaObjetivo = intervalo * i;
                
                // Buscar el depósito más cercano a esta distancia objetivo
                DepositoCandidato mejorCandidato = null;
                double menorDiferencia = Double.MAX_VALUE;

                for (DepositoCandidato candidato : candidatos) {
                    // Evitar depósitos ya seleccionados
                    if (seleccionados.contains(candidato.deposito)) {
                        continue;
                    }

                    double diferencia = Math.abs(candidato.distanciaDesdeOrigen - distanciaObjetivo);
                    if (diferencia < menorDiferencia) {
                        menorDiferencia = diferencia;
                        mejorCandidato = candidato;
                    }
                }

                if (mejorCandidato != null) {
                    seleccionados.add(mejorCandidato.deposito);
                }
            }
        }

        return seleccionados;
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * Retorna la distancia en kilómetros
     */
    private double calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int RADIO_TIERRA_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }

    /**
     * Clase interna para almacenar información de candidatos a depósitos
     */
    private static class DepositoCandidato {
        DepositoDTO deposito;
        double distanciaDesdeOrigen;
        double distanciaHaciaDestino;
        double porcentajeDesviacion;

        public DepositoCandidato(DepositoDTO deposito, double distanciaDesdeOrigen,
                                double distanciaHaciaDestino, double porcentajeDesviacion) {
            this.deposito = deposito;
            this.distanciaDesdeOrigen = distanciaDesdeOrigen;
            this.distanciaHaciaDestino = distanciaHaciaDestino;
            this.porcentajeDesviacion = porcentajeDesviacion;
        }
    }
}
