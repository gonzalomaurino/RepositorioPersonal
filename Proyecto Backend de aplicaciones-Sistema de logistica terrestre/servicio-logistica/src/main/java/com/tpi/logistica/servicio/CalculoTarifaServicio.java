package com.tpi.logistica.servicio;

import org.springframework.stereotype.Service;

@Service
public class CalculoTarifaServicio {


    private static final Double CARGO_GESTION_BASE = 5000.0;
    private static final Double COSTO_LITRO_COMBUSTIBLE = 1200.0;
    private static final Double COSTO_KM_BASE = 150.0;
    private static final Double VELOCIDAD_PROMEDIO_KMH = 60.0;

    public Double calcularCostoEstimadoTramo(Double distanciaKm, Double consumoPromedioCamiones) {
        Double cargoGestion = CARGO_GESTION_BASE;
        Double costoKm = distanciaKm * COSTO_KM_BASE;
        Double costoCombustible = distanciaKm * consumoPromedioCamiones * COSTO_LITRO_COMBUSTIBLE;

        return cargoGestion + costoKm + costoCombustible;
    }

    public Double calcularCostoRealTramo(Double distanciaKm, Double costoKmCamion, Double consumoCamion) {
        Double cargoGestion = CARGO_GESTION_BASE;
        Double costoKm = distanciaKm * costoKmCamion;
        Double costoCombustible = distanciaKm * consumoCamion * COSTO_LITRO_COMBUSTIBLE;

        return cargoGestion + costoKm + costoCombustible;
    }

    public Double calcularCostoEstadia(Long diasEstadia, Double costoEstadiaXdia) {
        return diasEstadia * costoEstadiaXdia;
    }

    public Double calcularTiempoEstimado(Double distanciaKm) {
        return distanciaKm / VELOCIDAD_PROMEDIO_KMH;
    }

    public Double calcularConsumoPromedio(java.util.List<Double> consumos) {
        if (consumos.isEmpty()) {
            return 0.1; 
        }
        return consumos.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.1);
    }
}

