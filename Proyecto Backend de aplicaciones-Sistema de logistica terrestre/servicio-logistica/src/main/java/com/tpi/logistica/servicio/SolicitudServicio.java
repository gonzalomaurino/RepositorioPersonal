package com.tpi.logistica.servicio;

import com.tpi.logistica.modelo.Solicitud;
import com.tpi.logistica.modelo.Ruta;
import com.tpi.logistica.modelo.Tramo;
import com.tpi.logistica.repositorio.SolicitudRepositorio;
import com.tpi.logistica.repositorio.RutaRepositorio;
import com.tpi.logistica.repositorio.TramoRepositorio;
import com.tpi.logistica.dto.EstimacionRutaRequest;
import com.tpi.logistica.dto.EstimacionRutaResponse;
import com.tpi.logistica.dto.SeguimientoSolicitudResponse;
import com.tpi.logistica.dto.ContenedorPendienteResponse;
import com.tpi.logistica.dto.SolicitudCompletaRequest;
import com.tpi.logistica.dto.SolicitudCompletaResponse;
import com.tpi.logistica.dto.DepositoDTO;
import com.tpi.logistica.dto.googlemaps.DistanciaYDuracion;
import com.tpi.logistica.config.MicroserviciosConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class SolicitudServicio {

    private static final Logger log = LoggerFactory.getLogger(SolicitudServicio.class);

    private final SolicitudRepositorio repositorio;
    private final RutaRepositorio rutaRepositorio;
    private final TramoRepositorio tramoRepositorio;
    private final CalculoTarifaServicio calculoTarifaServicio;
    private final GoogleMapsService googleMapsService;
    private final DepositoServicio depositoServicio;
    private final RestTemplate restTemplate;
    private final MicroserviciosConfig microserviciosConfig;

    public SolicitudServicio(SolicitudRepositorio repositorio,
                            RutaRepositorio rutaRepositorio,
                            TramoRepositorio tramoRepositorio,
                            CalculoTarifaServicio calculoTarifaServicio,
                            GoogleMapsService googleMapsService,
                            DepositoServicio depositoServicio,
                            RestTemplate restTemplate,
                            MicroserviciosConfig microserviciosConfig) {
        this.repositorio = repositorio;
        this.rutaRepositorio = rutaRepositorio;
        this.tramoRepositorio = tramoRepositorio;
        this.calculoTarifaServicio = calculoTarifaServicio;
        this.googleMapsService = googleMapsService;
        this.depositoServicio = depositoServicio;
        this.restTemplate = restTemplate;
        this.microserviciosConfig = microserviciosConfig;
    }

    public List<Solicitud> listar() {
        return repositorio.findAll();
    }

    public Optional<Solicitud> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Optional<Solicitud> buscarPorNumeroSeguimiento(String numeroSeguimiento) {
        return repositorio.findByNumeroSeguimiento(numeroSeguimiento);
    }

    public List<Solicitud> listarPorCliente(Long idCliente) {
        return repositorio.findByIdCliente(idCliente);
    }

    public List<Solicitud> listarPorEstado(String estado) {
        return repositorio.findByEstado(estado);
    }

    public Solicitud guardar(Solicitud nuevaSolicitud) {
        if (repositorio.existsByNumeroSeguimiento(nuevaSolicitud.getNumeroSeguimiento())) {
            throw new RuntimeException("Ya existe una solicitud con ese número de seguimiento");
        }
        

        Long idCliente = nuevaSolicitud.getIdCliente();
        validarOCrearCliente(idCliente);
        

        Long idContenedor = nuevaSolicitud.getIdContenedor();
        validarContenedor(idContenedor);
        

        if (nuevaSolicitud.getEstado() == null || nuevaSolicitud.getEstado().isEmpty()) {
            nuevaSolicitud.setEstado("BORRADOR");
        }
        
        return repositorio.save(nuevaSolicitud);
    }
    
    private void validarOCrearCliente(Long idCliente) {
        String urlGestion = microserviciosConfig.getServicioGestionUrl() + "/clientes/" + idCliente;
        
        try {
            log.debug("Validando cliente ID {} en URL: {}", idCliente, urlGestion);
            restTemplate.getForObject(urlGestion, ClienteDTO.class);
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Cliente ID {} no encontrado. Creando automáticamente...", idCliente);
            
            ClienteDTO nuevoCliente = new ClienteDTO();
            nuevoCliente.setNombre("Cliente");
            nuevoCliente.setApellido("AutoGenerado-" + idCliente);
            nuevoCliente.setEmail("cliente" + idCliente + "@autogenerado.com");
            nuevoCliente.setTelefono("+54-11-0000-0000");
            nuevoCliente.setCuil("20-" + String.format("%08d", idCliente) + "-0");
            
            String urlCrearCliente = microserviciosConfig.getServicioGestionUrl() + "/clientes";
            try {
                log.debug("Creando cliente automáticamente en URL: {}", urlCrearCliente);
                restTemplate.postForObject(urlCrearCliente, nuevoCliente, ClienteDTO.class);
                log.info("Cliente ID {} creado automáticamente", idCliente);
            } catch (Exception ex) {
                log.error("Error al crear cliente automáticamente en {}: {}", urlCrearCliente, ex.getMessage());
                throw new RuntimeException("Error al crear cliente automáticamente: " + ex.getMessage());
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Error de conexión al validar cliente en {}: {}", urlGestion, e.getMessage());
            throw new RuntimeException("Error de conexión al validar cliente con servicio-gestion en " + urlGestion + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al validar cliente en {}: {}", urlGestion, e.getMessage());
            throw new RuntimeException("Error al validar cliente con servicio-gestion en " + urlGestion + ": " + e.getMessage());
        }
    }
    
    private void validarContenedor(Long idContenedor) {
        String urlGestion = microserviciosConfig.getServicioGestionUrl() + "/contenedores/" + idContenedor;
        
        try {
            restTemplate.getForObject(urlGestion, ContenedorDTO.class);

            
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("El contenedor con ID " + idContenedor + " no existe. " +
                "Debe crear el contenedor antes de registrar la solicitud.");
                
        } catch (Exception e) {
            throw new RuntimeException("Error al validar contenedor con servicio-gestion: " + e.getMessage() + 
                ". Verifique que el servicio-gestion esté disponible en " + microserviciosConfig.getServicioGestionUrl());
        }
    }
    
    private static class ClienteDTO {
        private Long id;
        private String nombre;
        private String apellido;
        private String email;
        private String telefono;
        private String cuil;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getCuil() { return cuil; }
        public void setCuil(String cuil) { this.cuil = cuil; }
    }
    
    private static class ContenedorDTO {
        private Long id;
        private String codigoIdentificacion;
        private Double peso;
        private Double volumen;
        private ClienteDTO cliente;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCodigoIdentificacion() { return codigoIdentificacion; }
        public void setCodigoIdentificacion(String codigoIdentificacion) { this.codigoIdentificacion = codigoIdentificacion; }
        public Double getPeso() { return peso; }
        public void setPeso(Double peso) { this.peso = peso; }
        public Double getVolumen() { return volumen; }
        public void setVolumen(Double volumen) { this.volumen = volumen; }
        public ClienteDTO getCliente() { return cliente; }
        public void setCliente(ClienteDTO cliente) { this.cliente = cliente; }
    }
    
    private static class TarifaDTO {
        private Long id;
        private String descripcion;
        private Double rangoPesoMin;
        private Double rangoPesoMax;
        private Double rangoVolumenMin;
        private Double rangoVolumenMax;
        private Double valor;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Double getRangoPesoMin() { return rangoPesoMin; }
        public void setRangoPesoMin(Double rangoPesoMin) { this.rangoPesoMin = rangoPesoMin; }
        public Double getRangoPesoMax() { return rangoPesoMax; }
        public void setRangoPesoMax(Double rangoPesoMax) { this.rangoPesoMax = rangoPesoMax; }
        public Double getRangoVolumenMin() { return rangoVolumenMin; }
        public void setRangoVolumenMin(Double rangoVolumenMin) { this.rangoVolumenMin = rangoVolumenMin; }
        public Double getRangoVolumenMax() { return rangoVolumenMax; }
        public void setRangoVolumenMax(Double rangoVolumenMax) { this.rangoVolumenMax = rangoVolumenMax; }
        public Double getValor() { return valor; }
        public void setValor(Double valor) { this.valor = valor; }
    }

    public Solicitud actualizar(Long id, Solicitud datosActualizados) {
        return repositorio.findById(id)
                .map(solicitud -> {
                    solicitud.setNumeroSeguimiento(datosActualizados.getNumeroSeguimiento());
                    solicitud.setIdContenedor(datosActualizados.getIdContenedor());
                    solicitud.setIdCliente(datosActualizados.getIdCliente());
                    solicitud.setOrigenDireccion(datosActualizados.getOrigenDireccion());
                    solicitud.setOrigenLatitud(datosActualizados.getOrigenLatitud());
                    solicitud.setOrigenLongitud(datosActualizados.getOrigenLongitud());
                    solicitud.setDestinoDireccion(datosActualizados.getDestinoDireccion());
                    solicitud.setDestinoLatitud(datosActualizados.getDestinoLatitud());
                    solicitud.setDestinoLongitud(datosActualizados.getDestinoLongitud());
                    solicitud.setEstado(datosActualizados.getEstado());
                    solicitud.setCostoEstimado(datosActualizados.getCostoEstimado());
                    solicitud.setTiempoEstimado(datosActualizados.getTiempoEstimado());
                    solicitud.setCostoFinal(datosActualizados.getCostoFinal());
                    solicitud.setTiempoReal(datosActualizados.getTiempoReal());
                    return repositorio.save(solicitud);
                })
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
    }

    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RuntimeException("Solicitud no encontrada con ID: " + id);
        }
        repositorio.deleteById(id);
    }

    /**
     * Crea una solicitud completa incluyendo la creación automática del cliente y contenedor si no existen.
     * Este método implementa el requerimiento de crear la solicitud junto con el contenedor y cliente.
     * 
     * @param request DTO con todos los datos necesarios
     * @return Response con los IDs generados y banderas indicando qué se creó
     */
    @Transactional
    public SolicitudCompletaResponse crearSolicitudCompleta(SolicitudCompletaRequest request) {
        boolean clienteCreado = false;
        boolean contenedorCreado = false;
        Long idCliente;
        Long idContenedor;

        // ========== PASO 1: Validar o crear el CLIENTE ==========
        if (request.getIdCliente() != null) {
            // Si se proporciona ID de cliente, validar que exista
            idCliente = request.getIdCliente();
            validarOCrearCliente(idCliente);
        } else {
            // Si no se proporciona ID, crear cliente con los datos del request
            if (request.getClienteNombre() == null || request.getClienteApellido() == null) {
                throw new RuntimeException("Debe proporcionar el ID del cliente o los datos completos (nombre, apellido, email) para crear uno nuevo");
            }
            
            idCliente = crearCliente(
                request.getClienteNombre(),
                request.getClienteApellido(),
                request.getClienteEmail(),
                request.getClienteTelefono(),
                request.getClienteCuil()
            );
            clienteCreado = true;
        }

        // ========== PASO 2: Validar o crear el CONTENEDOR ==========
        if (request.getIdContenedor() != null) {
            // Si se proporciona ID de contenedor, validar que exista
            idContenedor = request.getIdContenedor();
            validarContenedor(idContenedor);
        } else {
            // Si no se proporciona ID, crear contenedor con los datos del request
            if (request.getCodigoIdentificacion() == null || request.getPeso() == null || request.getVolumen() == null) {
                throw new RuntimeException("Debe proporcionar el ID del contenedor o los datos completos (código, peso, volumen) para crear uno nuevo");
            }
            
            idContenedor = crearContenedor(
                request.getCodigoIdentificacion(),
                request.getPeso(),
                request.getVolumen(),
                idCliente
            );
            contenedorCreado = true;
        }

        // ========== PASO 3: Crear la SOLICITUD ==========
        Solicitud nuevaSolicitud = Solicitud.builder()
            .numeroSeguimiento(request.getNumeroSeguimiento())
            .idCliente(idCliente)
            .idContenedor(idContenedor)
            .origenDireccion(request.getOrigenDireccion())
            .origenLatitud(request.getOrigenLatitud())
            .origenLongitud(request.getOrigenLongitud())
            .destinoDireccion(request.getDestinoDireccion())
            .destinoLatitud(request.getDestinoLatitud())
            .destinoLongitud(request.getDestinoLongitud())
            .estado("BORRADOR")
            .build();

        if (repositorio.existsByNumeroSeguimiento(nuevaSolicitud.getNumeroSeguimiento())) {
            throw new RuntimeException("Ya existe una solicitud con ese número de seguimiento");
        }

        Solicitud solicitudGuardada = repositorio.save(nuevaSolicitud);

        // ========== PASO 4: Construir respuesta ==========
        String mensaje = String.format(
            "✅ Solicitud creada exitosamente. %s %s",
            clienteCreado ? "Cliente creado automáticamente." : "Cliente existente utilizado.",
            contenedorCreado ? "Contenedor creado automáticamente." : "Contenedor existente utilizado."
        );

        return SolicitudCompletaResponse.builder()
            .idSolicitud(solicitudGuardada.getId())
            .numeroSeguimiento(solicitudGuardada.getNumeroSeguimiento())
            .estado(solicitudGuardada.getEstado())
            .idCliente(idCliente)
            .clienteCreado(clienteCreado)
            .idContenedor(idContenedor)
            .codigoIdentificacion(request.getCodigoIdentificacion())
            .contenedorCreado(contenedorCreado)
            .origenDireccion(request.getOrigenDireccion())
            .destinoDireccion(request.getDestinoDireccion())
            .mensaje(mensaje)
            .build();
    }

    /**
     * Crea un nuevo cliente en el servicio de gestión.
     * 
     * @return ID del cliente creado
     */
    private Long crearCliente(String nombre, String apellido, String email, String telefono, String cuil) {
        String urlGestion = microserviciosConfig.getServicioGestionUrl() + "/clientes";
        
        ClienteDTO nuevoCliente = new ClienteDTO();
        nuevoCliente.setNombre(nombre);
        nuevoCliente.setApellido(apellido);
        nuevoCliente.setEmail(email != null ? email : "cliente-" + System.currentTimeMillis() + "@autogenerado.com");
        nuevoCliente.setTelefono(telefono != null ? telefono : "+54-11-0000-0000");
        nuevoCliente.setCuil(cuil != null ? cuil : "20-00000000-0");
        
        try {
            log.debug("Creando cliente en URL: {}", urlGestion);
            ClienteDTO clienteCreado = restTemplate.postForObject(urlGestion, nuevoCliente, ClienteDTO.class);
            if (clienteCreado != null && clienteCreado.getId() != null) {
                log.info("Cliente creado automáticamente con ID: {}", clienteCreado.getId());
                return clienteCreado.getId();
            } else {
                throw new RuntimeException("Error: El servicio de gestión no retornó el ID del cliente creado");
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Error de conexión al crear cliente en {}: {}", urlGestion, e.getMessage());
            throw new RuntimeException("Error de conexión al crear cliente en servicio-gestion en " + urlGestion + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al crear cliente en {}: {}", urlGestion, e.getMessage());
            throw new RuntimeException("Error al crear cliente en servicio-gestion en " + urlGestion + ": " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo contenedor en el servicio de gestión.
     * 
     * @return ID del contenedor creado
     */
    private Long crearContenedor(String codigoIdentificacion, Double peso, Double volumen, Long idCliente) {
        String urlGestion = microserviciosConfig.getServicioGestionUrl() + "/contenedores";
        
        ContenedorDTO nuevoContenedor = new ContenedorDTO();
        nuevoContenedor.setCodigoIdentificacion(codigoIdentificacion);
        nuevoContenedor.setPeso(peso);
        nuevoContenedor.setVolumen(volumen);
        
        // El contenedor necesita el cliente asociado
        ClienteDTO cliente = new ClienteDTO();
        cliente.setId(idCliente);
        nuevoContenedor.setCliente(cliente);
        
        try {
            ContenedorDTO contenedorCreado = restTemplate.postForObject(urlGestion, nuevoContenedor, ContenedorDTO.class);
            if (contenedorCreado != null && contenedorCreado.getId() != null) {
                log.info("Contenedor creado automáticamente con ID: {} y código: {}", 
                    contenedorCreado.getId(), codigoIdentificacion);
                return contenedorCreado.getId();
            } else {
                throw new RuntimeException("Error: El servicio de gestión no retornó el ID del contenedor creado");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al crear contenedor en servicio-gestion: " + e.getMessage());
        }
    }

    public EstimacionRutaResponse estimarRuta(EstimacionRutaRequest request) {
        // Si se proporcionan peso y volumen, validar que haya tarifa aplicable
        if (request.getPesoKg() != null && request.getVolumenM3() != null) {
            String urlGestion = microserviciosConfig.getServicioGestionUrl() + 
                "/tarifas/aplicable?peso=" + request.getPesoKg() + "&volumen=" + request.getVolumenM3();
            
            try {
                TarifaDTO tarifa = restTemplate.getForObject(urlGestion, TarifaDTO.class);
                
                if (tarifa == null) {
                    throw new RuntimeException("Tarifa no encontrada aplicable para peso " + 
                        request.getPesoKg() + " y volumen " + request.getVolumenM3());
                }
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                throw new RuntimeException("Tarifa no encontrada aplicable para peso " + 
                    request.getPesoKg() + " y volumen " + request.getVolumenM3());
            } catch (Exception e) {
                if (e instanceof RuntimeException && e.getMessage().contains("Tarifa no encontrada")) {
                    throw e;
                }
                // Si hay otro error, continuar (no crítico para la estimación)
                log.warn("Error al validar tarifa aplicable: {}", e.getMessage());
            }
        }

        DistanciaYDuracion distancia;

        if (request.getOrigenLatitud() != null && request.getOrigenLongitud() != null &&
            request.getDestinoLatitud() != null && request.getDestinoLongitud() != null) {

            distancia = googleMapsService.calcularDistanciaPorCoordenadas(
                request.getOrigenLatitud(), request.getOrigenLongitud(),
                request.getDestinoLatitud(), request.getDestinoLongitud()
            );
        } else {

            distancia = googleMapsService.calcularDistanciaYDuracion(
                request.getOrigenDireccion(),
                request.getDestinoDireccion()
            );
        }

        Double distanciaKm = distancia.getDistanciaKm();
        Double tiempoEstimado = distancia.getDuracionHoras();
        Double consumoPromedio = 0.15; 

        Double costoEstimado = calculoTarifaServicio.calcularCostoEstimadoTramo(distanciaKm, consumoPromedio);

        EstimacionRutaResponse.TramoEstimado tramo = EstimacionRutaResponse.TramoEstimado.builder()
                .origenDescripcion(distancia.getOrigenDireccion())
                .destinoDescripcion(distancia.getDestinoDireccion())
                .distanciaKm(distanciaKm)
                .costoEstimado(costoEstimado)
                .tiempoEstimadoHoras(tiempoEstimado)
                .build();

        return EstimacionRutaResponse.builder()
                .costoEstimado(costoEstimado)
                .tiempoEstimadoHoras(tiempoEstimado)
                .tramos(List.of(tramo))
                .build();
    }

    @Transactional
    public Solicitud asignarRuta(Long idSolicitud, EstimacionRutaRequest datosRuta) {
        Solicitud solicitud = repositorio.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + idSolicitud));

        if (!"BORRADOR".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden asignar rutas a solicitudes en estado BORRADOR");
        }

        // Crear la ruta
        Ruta ruta = Ruta.builder()
                .idSolicitud(idSolicitud)
                .build();
        ruta = rutaRepositorio.save(ruta);

        // Buscar depósitos intermedios en la ruta
        List<DepositoDTO> depositosIntermedios = List.of();
        if (solicitud.getOrigenLatitud() != null && solicitud.getOrigenLongitud() != null &&
            solicitud.getDestinoLatitud() != null && solicitud.getDestinoLongitud() != null) {
            
            depositosIntermedios = depositoServicio.buscarDepositosEnRuta(
                solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud(),
                solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud()
            );
            
            log.info("Depósitos intermedios encontrados: {}", depositosIntermedios.size());
        }

        // Crear tramos según los depósitos encontrados
        List<Tramo> tramosCreados = new ArrayList<>();
        Double costoTotalEstimado = 0.0;
        Double tiempoTotalEstimado = 0.0;
        LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);

        if (depositosIntermedios.isEmpty()) {
            // Sin depósitos intermedios: crear un solo tramo directo
            log.info("No hay depósitos intermedios, creando tramo directo");
            
            DistanciaYDuracion distancia = calcularDistanciaTramo(
                solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud(),
                solicitud.getOrigenDireccion(),
                solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud(),
                solicitud.getDestinoDireccion()
            );

            Double costoEstimado = calculoTarifaServicio.calcularCostoEstimadoTramo(
                distancia.getDistanciaKm(), 0.15);

            Tramo tramo = Tramo.builder()
                    .idRuta(ruta.getId())
                    .origenDescripcion(distancia.getOrigenDireccion())
                    .destinoDescripcion(distancia.getDestinoDireccion())
                    .distanciaKm(distancia.getDistanciaKm())
                    .costoEstimado(costoEstimado)
                    .estado("ESTIMADO")
                    .fechaInicioEstimada(fechaInicio)
                    .fechaFinEstimada(fechaInicio.plusHours(distancia.getDuracionHoras().longValue()))
                    .build();
            
            tramosCreados.add(tramoRepositorio.save(tramo));
            costoTotalEstimado = costoEstimado;
            tiempoTotalEstimado = distancia.getDuracionHoras();

        } else {
            // Con depósitos intermedios: crear múltiples tramos
            log.info("Creando {} tramos con depósitos intermedios", depositosIntermedios.size() + 1);

            // Crear lista de puntos de la ruta (origen -> depósitos -> destino)
            List<PuntoRuta> puntosRuta = new ArrayList<>();
            
            // Punto origen
            puntosRuta.add(new PuntoRuta(
                solicitud.getOrigenDireccion(),
                solicitud.getOrigenLatitud(),
                solicitud.getOrigenLongitud()
            ));
            
            // Depósitos intermedios
            for (DepositoDTO deposito : depositosIntermedios) {
                puntosRuta.add(new PuntoRuta(
                    deposito.getDireccion() + " (" + deposito.getNombre() + ")",
                    deposito.getLatitud(),
                    deposito.getLongitud()
                ));
            }
            
            // Punto destino
            puntosRuta.add(new PuntoRuta(
                solicitud.getDestinoDireccion(),
                solicitud.getDestinoLatitud(),
                solicitud.getDestinoLongitud()
            ));

            // Crear un tramo por cada segmento
            LocalDateTime fechaActual = fechaInicio;
            
            for (int i = 0; i < puntosRuta.size() - 1; i++) {
                PuntoRuta puntoOrigen = puntosRuta.get(i);
                PuntoRuta puntoDestino = puntosRuta.get(i + 1);

                log.info("Creando tramo {}: {} -> {}", (i + 1), 
                    puntoOrigen.descripcion, puntoDestino.descripcion);

                DistanciaYDuracion distancia = calcularDistanciaTramo(
                    puntoOrigen.latitud, puntoOrigen.longitud, puntoOrigen.descripcion,
                    puntoDestino.latitud, puntoDestino.longitud, puntoDestino.descripcion
                );

                Double costoEstimado = calculoTarifaServicio.calcularCostoEstimadoTramo(
                    distancia.getDistanciaKm(), 0.15);

                LocalDateTime fechaFin = fechaActual.plusHours(distancia.getDuracionHoras().longValue());

                Tramo tramo = Tramo.builder()
                        .idRuta(ruta.getId())
                        .origenDescripcion(distancia.getOrigenDireccion())
                        .destinoDescripcion(distancia.getDestinoDireccion())
                        .distanciaKm(distancia.getDistanciaKm())
                        .costoEstimado(costoEstimado)
                        .estado("ESTIMADO")
                        .fechaInicioEstimada(fechaActual)
                        .fechaFinEstimada(fechaFin)
                        .build();
                
                tramosCreados.add(tramoRepositorio.save(tramo));
                
                costoTotalEstimado += costoEstimado;
                tiempoTotalEstimado += distancia.getDuracionHoras();
                
                // Agregar tiempo de parada en depósito (1 hora)
                fechaActual = fechaFin.plusHours(1);
            }
        }

        log.info("Total de tramos creados: {}, Costo total: {}, Tiempo total: {}h",
            tramosCreados.size(), costoTotalEstimado, tiempoTotalEstimado);

        // Actualizar solicitud
        solicitud.setEstado("PROGRAMADA");
        solicitud.setCostoEstimado(costoTotalEstimado);
        solicitud.setTiempoEstimado(tiempoTotalEstimado);

        return repositorio.save(solicitud);
    }

    /**
     * Calcula la distancia y duración de un tramo
     */
    private DistanciaYDuracion calcularDistanciaTramo(
            Double origenLat, Double origenLng, String origenDesc,
            Double destinoLat, Double destinoLng, String destinoDesc) {
        
        if (origenLat != null && origenLng != null && destinoLat != null && destinoLng != null) {
            return googleMapsService.calcularDistanciaPorCoordenadas(
                origenLat, origenLng, destinoLat, destinoLng);
        } else {
            return googleMapsService.calcularDistanciaYDuracion(origenDesc, destinoDesc);
        }
    }

    /**
     * Clase auxiliar para representar un punto en la ruta
     */
    private static class PuntoRuta {
        String descripcion;
        Double latitud;
        Double longitud;

        PuntoRuta(String descripcion, Double latitud, Double longitud) {
            this.descripcion = descripcion;
            this.latitud = latitud;
            this.longitud = longitud;
        }
    }

    public List<ContenedorPendienteResponse> listarPendientes(String estadoFiltro, Long idContenedor) {
        List<Solicitud> solicitudes;
        
        if (idContenedor != null) {

            solicitudes = repositorio.findByIdContenedor(idContenedor).stream()
                    .filter(s -> !esEstadoFinal(s.getEstado()))
                    .toList();
        } else if (estadoFiltro != null && !estadoFiltro.isEmpty()) {

            solicitudes = repositorio.findByEstado(estadoFiltro);
        } else {

            solicitudes = repositorio.findAll().stream()
                    .filter(s -> !esEstadoFinal(s.getEstado()))
                    .toList();
        }
        
        return solicitudes.stream()
                .map(this::convertirAContenedorPendiente)
                .toList();
    }
    
    private boolean esEstadoFinal(String estado) {
        if (estado == null) return false;
        String estadoLower = estado.toLowerCase();
        return estadoLower.equals("completada") || 
               estadoLower.equals("cancelada") || 
               estadoLower.equals("entregada");
    }

    private ContenedorPendienteResponse convertirAContenedorPendiente(Solicitud solicitud) {

        List<Ruta> rutas = rutaRepositorio.findByIdSolicitud(solicitud.getId());
        
        ContenedorPendienteResponse.ContenedorPendienteResponseBuilder builder = 
                ContenedorPendienteResponse.builder()
                .idSolicitud(solicitud.getId())
                .numeroSeguimiento(solicitud.getNumeroSeguimiento())
                .idContenedor(solicitud.getIdContenedor())
                .idCliente(solicitud.getIdCliente())
                .estado(solicitud.getEstado())
                .costoEstimado(solicitud.getCostoEstimado())
                .costoFinal(solicitud.getCostoFinal());
        

        if (!rutas.isEmpty()) {
            Ruta ruta = rutas.get(0);
            List<Tramo> tramos = tramoRepositorio.findByIdRuta(ruta.getId());
            

            Optional<Tramo> tramoActivo = tramos.stream()
                    .filter(t -> "INICIADO".equals(t.getEstado()) || "ASIGNADO".equals(t.getEstado()))
                    .findFirst();
            
            if (tramoActivo.isPresent()) {
                Tramo tramo = tramoActivo.get();
                
                if ("INICIADO".equals(tramo.getEstado())) {
                    builder.ubicacionActual("EN_TRANSITO")
                           .descripcionUbicacion("En viaje de " + tramo.getOrigenDescripcion() + 
                                                " hacia " + tramo.getDestinoDescripcion());
                } else {
                    builder.ubicacionActual("EN_DEPOSITO")
                           .descripcionUbicacion("En depósito: " + tramo.getOrigenDescripcion());
                }
                
                builder.tramoActual(ContenedorPendienteResponse.TramoActual.builder()
                        .idTramo(tramo.getId())
                        .origen(tramo.getOrigenDescripcion())
                        .destino(tramo.getDestinoDescripcion())
                        .estadoTramo(tramo.getEstado())
                        .patenteCamion(tramo.getPatenteCamion())
                        .build());
            } else {

                Optional<Tramo> ultimoFinalizado = tramos.stream()
                        .filter(t -> "FINALIZADO".equals(t.getEstado()))
                        .reduce((first, second) -> second); 
                
                if (ultimoFinalizado.isPresent()) {
                    builder.ubicacionActual("EN_DEPOSITO")
                           .descripcionUbicacion("En depósito: " + 
                                                ultimoFinalizado.get().getDestinoDescripcion());
                } else {
                    builder.ubicacionActual("PENDIENTE_ASIGNACION")
                           .descripcionUbicacion("Pendiente de asignación de camión");
                }
            }
        } else {
            builder.ubicacionActual("ORIGEN")
                   .descripcionUbicacion("En punto de origen: " + solicitud.getOrigenDireccion());
        }
        
        return builder.build();
    }

    public SeguimientoSolicitudResponse obtenerSeguimiento(String numeroSeguimiento) {
        Solicitud solicitud = repositorio.findByNumeroSeguimiento(numeroSeguimiento)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con número de seguimiento: " + numeroSeguimiento));


        List<Ruta> rutas = rutaRepositorio.findByIdSolicitud(solicitud.getId());
        List<SeguimientoSolicitudResponse.EventoSeguimiento> historial = new ArrayList<>();


        historial.add(SeguimientoSolicitudResponse.EventoSeguimiento.builder()
                .fecha(LocalDateTime.now().minusDays(5)) 
                .evento("SOLICITUD_CREADA")
                .descripcion("Solicitud creada en el sistema")
                .estado("BORRADOR")
                .build());


        if (!rutas.isEmpty()) {
            Ruta ruta = rutas.get(0);
            List<Tramo> tramos = tramoRepositorio.findByIdRuta(ruta.getId());

            historial.add(SeguimientoSolicitudResponse.EventoSeguimiento.builder()
                    .fecha(LocalDateTime.now().minusDays(4)) 
                    .evento("RUTA_ASIGNADA")
                    .descripcion("Ruta calculada con " + tramos.size() + " tramo(s)")
                    .estado("PROGRAMADA")
                    .build());


            for (Tramo tramo : tramos) {
                if (tramo.getFechaInicioReal() != null) {
                    historial.add(SeguimientoSolicitudResponse.EventoSeguimiento.builder()
                            .fecha(tramo.getFechaInicioReal())
                            .evento("TRAMO_INICIADO")
                            .descripcion("Inicio de tramo: " + tramo.getOrigenDescripcion() +
                                       " → " + tramo.getDestinoDescripcion())
                            .estado("EN_TRANSITO")
                            .build());
                }

                if (tramo.getFechaFinReal() != null) {
                    historial.add(SeguimientoSolicitudResponse.EventoSeguimiento.builder()
                            .fecha(tramo.getFechaFinReal())
                            .evento("TRAMO_FINALIZADO")
                            .descripcion("Fin de tramo: " + tramo.getOrigenDescripcion() +
                                       " → " + tramo.getDestinoDescripcion())
                            .estado(tramo.getEstado())
                            .build());
                }
            }
        }


        historial.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));

        return SeguimientoSolicitudResponse.builder()
                .idSolicitud(solicitud.getId())
                .numeroSeguimiento(solicitud.getNumeroSeguimiento())
                .estadoActual(solicitud.getEstado())
                .costoEstimado(solicitud.getCostoEstimado())
                .costoFinal(solicitud.getCostoFinal())
                .tiempoEstimadoHoras(solicitud.getTiempoEstimado())
                .tiempoRealHoras(solicitud.getTiempoReal())
                .historial(historial)
                .build();
    }
}
