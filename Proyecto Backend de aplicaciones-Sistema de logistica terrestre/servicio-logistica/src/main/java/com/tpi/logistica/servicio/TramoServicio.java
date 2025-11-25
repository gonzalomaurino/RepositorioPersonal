package com.tpi.logistica.servicio;

import com.tpi.logistica.modelo.Tramo;
import com.tpi.logistica.repositorio.TramoRepositorio;
import com.tpi.logistica.repositorio.SolicitudRepositorio;
import com.tpi.logistica.repositorio.RutaRepositorio;
import com.tpi.logistica.config.MicroserviciosConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class TramoServicio {

    private static final Logger log = LoggerFactory.getLogger(TramoServicio.class);

    private final TramoRepositorio repositorio;
    private final SolicitudRepositorio solicitudRepositorio;
    private final RutaRepositorio rutaRepositorio;
    private final CalculoTarifaServicio calculoTarifaServicio;
    private final RestTemplate restTemplate;
    private final MicroserviciosConfig microserviciosConfig;

    public TramoServicio(TramoRepositorio repositorio,
                        SolicitudRepositorio solicitudRepositorio,
                        RutaRepositorio rutaRepositorio,
                        CalculoTarifaServicio calculoTarifaServicio,
                        RestTemplate restTemplate,
                        MicroserviciosConfig microserviciosConfig) {
        this.repositorio = repositorio;
        this.solicitudRepositorio = solicitudRepositorio;
        this.rutaRepositorio = rutaRepositorio;
        this.calculoTarifaServicio = calculoTarifaServicio;
        this.restTemplate = restTemplate;
        this.microserviciosConfig = microserviciosConfig;
    }

    public List<Tramo> listar() {
        return repositorio.findAll();
    }

    public Optional<Tramo> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public List<Tramo> listarPorRuta(Long idRuta) {
        return repositorio.findByIdRuta(idRuta);
    }

    public List<Tramo> listarPorCamion(String patenteCamion) {
        return repositorio.findByPatenteCamion(patenteCamion);
    }

    public List<Tramo> listarPorEstado(String estado) {
        return repositorio.findByEstado(estado);
    }

    public Tramo guardar(Tramo nuevoTramo) {
        return repositorio.save(nuevoTramo);
    }

    public Tramo actualizar(Long id, Tramo datosActualizados) {
        return repositorio.findById(id)
                .map(tramo -> {
                    tramo.setIdRuta(datosActualizados.getIdRuta());
                    tramo.setPatenteCamion(datosActualizados.getPatenteCamion());
                    tramo.setOrigenDescripcion(datosActualizados.getOrigenDescripcion());
                    tramo.setDestinoDescripcion(datosActualizados.getDestinoDescripcion());
                    tramo.setDistanciaKm(datosActualizados.getDistanciaKm());
                    tramo.setEstado(datosActualizados.getEstado());
                    tramo.setFechaInicioEstimada(datosActualizados.getFechaInicioEstimada());
                    tramo.setFechaFinEstimada(datosActualizados.getFechaFinEstimada());
                    tramo.setFechaInicioReal(datosActualizados.getFechaInicioReal());
                    tramo.setFechaFinReal(datosActualizados.getFechaFinReal());
                    return repositorio.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + id));
    }

    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RuntimeException("Tramo no encontrado con ID: " + id);
        }
        repositorio.deleteById(id);
    }

    @Transactional
    public Tramo asignarCamion(Long idTramo, String patenteCamion, Double pesoContenedor, Double volumenContenedor) {
        Tramo tramo = repositorio.findById(idTramo)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + idTramo));


        if (!"ESTIMADO".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden asignar camiones a tramos en estado ESTIMADO");
        }


        String urlFlota = microserviciosConfig.getServicioFlotaUrl() + "/camiones/aptos?peso=" + pesoContenedor + "&volumen=" + volumenContenedor;
        
        try {

            CamionDTO[] camionesAptos = restTemplate.getForObject(urlFlota, CamionDTO[].class);
            
            if (camionesAptos == null || camionesAptos.length == 0) {
                throw new RuntimeException("No hay camiones disponibles con capacidad suficiente para este contenedor " +
                    "(peso: " + pesoContenedor + "kg, volumen: " + volumenContenedor + "m³)");
            }
            
            
            boolean camionApto = Arrays.stream(camionesAptos)
                .anyMatch(c -> c.getPatente().equals(patenteCamion));
            
            if (!camionApto) {
                throw new RuntimeException("El camión " + patenteCamion + 
                    " no tiene capacidad suficiente para transportar este contenedor " +
                    "(peso: " + pesoContenedor + "kg, volumen: " + volumenContenedor + "m³). " +
                    "Camiones disponibles aptos: " + Arrays.stream(camionesAptos)
                        .map(CamionDTO::getPatente)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("ninguno"));
            }
            
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error al consultar capacidad del camión en servicio-flota: " + e.getMessage() + 
                ". Verifique que el servicio-flota esté disponible en " + microserviciosConfig.getServicioFlotaUrl());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e; 
            }
            throw new RuntimeException("Error inesperado al validar capacidad del camión: " + e.getMessage());
        }


        tramo.setPatenteCamion(patenteCamion);
        tramo.setEstado("ASIGNADO");

        return repositorio.save(tramo);
    }
    
    private static class CamionDTO {
        private String patente;
        private Double capacidadPeso;
        private Double capacidadVolumen;
        private Boolean disponible;
        
        public String getPatente() { return patente; }
        public void setPatente(String patente) { this.patente = patente; }
        public Double getCapacidadPeso() { return capacidadPeso; }
        public void setCapacidadPeso(Double capacidadPeso) { this.capacidadPeso = capacidadPeso; }
        public Double getCapacidadVolumen() { return capacidadVolumen; }
        public void setCapacidadVolumen(Double capacidadVolumen) { this.capacidadVolumen = capacidadVolumen; }
        public Boolean getDisponible() { return disponible; }
        public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    }

    @Transactional
    public Tramo iniciarTramo(Long idTramo) {
        Tramo tramo = repositorio.findById(idTramo)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + idTramo));

        if (!"ASIGNADO".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden iniciar tramos en estado ASIGNADO");
        }

        tramo.setFechaInicioReal(LocalDateTime.now());
        tramo.setEstado("INICIADO");

        return repositorio.save(tramo);
    }

    @Transactional
    public Tramo finalizarTramo(Long idTramo, Double kmReales, Double costoKmCamion, Double consumoCamion) {
        Tramo tramo = repositorio.findById(idTramo)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + idTramo));

        if (kmReales == null || kmReales <= 0) {
            throw new IllegalArgumentException("Los kilómetros reales deben ser mayores a 0");
        }
        if (costoKmCamion == null || costoKmCamion <= 0) {
            throw new IllegalArgumentException("El costo por km del camión debe ser mayor a 0");
        }
        if (consumoCamion == null || consumoCamion <= 0) {
            throw new IllegalArgumentException("El consumo del camión debe ser mayor a 0");
        }

        if (!"INICIADO".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden finalizar tramos en estado INICIADO");
        }

        tramo.setFechaFinReal(LocalDateTime.now());
        tramo.setDistanciaKm(kmReales); 
        tramo.setEstado("FINALIZADO");


        Double costoReal = calculoTarifaServicio.calcularCostoRealTramo(kmReales, costoKmCamion, consumoCamion);
        tramo.setCostoReal(costoReal);

        tramo = repositorio.save(tramo);


        List<Tramo> tramosRuta = repositorio.findByIdRuta(tramo.getIdRuta());
        boolean todosFinalizados = tramosRuta.stream()
                .allMatch(t -> "FINALIZADO".equals(t.getEstado()));

        if (todosFinalizados) {

            actualizarSolicitudFinal(tramo.getIdRuta(), tramosRuta);
        }

        return tramo;
    }

    private void actualizarSolicitudFinal(Long idRuta, List<Tramo> tramos) {


        final Duration[] tiempoTotal = {Duration.ZERO};
        final Double[] costoTotal = {0.0};

        for (Tramo t : tramos) {
            if (t.getFechaInicioReal() != null && t.getFechaFinReal() != null) {
                tiempoTotal[0] = tiempoTotal[0].plus(
                    Duration.between(t.getFechaInicioReal(), t.getFechaFinReal())
                );
            }
            if (t.getCostoReal() != null) {
                costoTotal[0] += t.getCostoReal();
            }
        }

        // Calcular el costo de estadías en depósitos entre tramos consecutivos
        Double costoEstadias = calcularEstadiasEnDepositos(tramos);
        costoTotal[0] += costoEstadias;

        rutaRepositorio.findById(idRuta).ifPresent(ruta -> {
            solicitudRepositorio.findById(ruta.getIdSolicitud()).ifPresent(solicitud -> {

                if ("PROGRAMADA".equals(solicitud.getEstado()) || "EN_TRANSITO".equals(solicitud.getEstado())) {
                    solicitud.setTiempoReal(tiempoTotal[0].toHours() + (tiempoTotal[0].toMinutesPart() / 60.0));
                    solicitud.setCostoFinal(costoTotal[0]);
                    solicitud.setEstado("ENTREGADA");
                    solicitudRepositorio.save(solicitud);
                    
                    log.info("Solicitud ID {} marcada como ENTREGADA", solicitud.getId());
                    log.debug("   - Costo final: ${}", costoTotal[0]);
                    log.debug("   - Costo estadías: ${}", costoEstadias);
                    log.debug("   - Tiempo real: {} horas", solicitud.getTiempoReal());
                }
            });
        });
    }

    /**
     * Calcula el costo de estadías en depósitos entre tramos consecutivos.
     * Se considera estadía el tiempo entre la finalización de un tramo y el inicio del siguiente.
     * La consigna requiere: "Estadía en depósitos (calculada a partir de la diferencia 
     * entre fechas reales de entrada y salida del tramo correspondiente)"
     * 
     * @param tramos Lista de tramos de la ruta
     * @return Costo total de estadías
     */
    private Double calcularEstadiasEnDepositos(List<Tramo> tramos) {
        if (tramos == null || tramos.size() <= 1) {
            return 0.0;
        }

        // Ordenar tramos por fecha de inicio real
        List<Tramo> tramosOrdenados = tramos.stream()
            .filter(t -> t.getFechaInicioReal() != null && t.getFechaFinReal() != null)
            .sorted(Comparator.comparing(Tramo::getFechaInicioReal))
            .toList();

        if (tramosOrdenados.size() <= 1) {
            return 0.0;
        }

        Double costoTotalEstadias = 0.0;
        // Costo estándar por día de estadía en depósito
        // La consigna indica: "Cada depósito debe mantener un costo de estadía diario"
        // Usamos un valor fijo de $500/día como configuración estándar
        final Double COSTO_ESTADIA_DIA = 500.0;

        // Calcular estadías entre tramos consecutivos
        for (int i = 0; i < tramosOrdenados.size() - 1; i++) {
            Tramo tramoActual = tramosOrdenados.get(i);
            Tramo tramoSiguiente = tramosOrdenados.get(i + 1);

            LocalDateTime finTramoActual = tramoActual.getFechaFinReal();
            LocalDateTime inicioTramoSiguiente = tramoSiguiente.getFechaInicioReal();

            // Calcular duración de la estadía
            Duration duracionEstadia = Duration.between(finTramoActual, inicioTramoSiguiente);
            
            // Si hay estadía (tiempo positivo entre tramos)
            if (duracionEstadia.toHours() > 0) {
                // Calcular días de estadía (redondear hacia arriba)
                double diasEstadia = Math.ceil(duracionEstadia.toHours() / 24.0);
                Double costoEstadia = diasEstadia * COSTO_ESTADIA_DIA;
                costoTotalEstadias += costoEstadia;
                
                log.info("Estadía calculada entre {} y {}", 
                    tramoActual.getDestinoDescripcion(), tramoSiguiente.getOrigenDescripcion());
                log.debug("   - Duración: {} horas ({} días)", duracionEstadia.toHours(), diasEstadia);
                log.debug("   - Costo: ${}", costoEstadia);
            }
        }

        return costoTotalEstadias;
    }
}
