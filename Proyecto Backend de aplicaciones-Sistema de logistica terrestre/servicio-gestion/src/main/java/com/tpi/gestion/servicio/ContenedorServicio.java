package com.tpi.gestion.servicio;

import com.tpi.gestion.modelo.Cliente;
import com.tpi.gestion.modelo.Contenedor;
import com.tpi.gestion.repositorio.ClienteRepositorio;
import com.tpi.gestion.repositorio.ContenedorRepositorio;
import com.tpi.gestion.dto.EstadoContenedorResponse;
import com.tpi.gestion.dto.SolicitudLogisticaDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContenedorServicio {

    private final ContenedorRepositorio contenedorRepo;
    private final ClienteRepositorio clienteRepo;
    private final LogisticaClienteServicio logisticaCliente;

    public ContenedorServicio(ContenedorRepositorio contenedorRepo, 
                             ClienteRepositorio clienteRepo,
                             LogisticaClienteServicio logisticaCliente) {
        this.contenedorRepo = contenedorRepo;
        this.clienteRepo = clienteRepo;
        this.logisticaCliente = logisticaCliente;
    }

    public List<Contenedor> listar() {
        return contenedorRepo.findAll();
    }

    public List<Contenedor> listarPorCliente(Long idCliente) {
        return contenedorRepo.findByClienteId(idCliente);
    }

    public Optional<Contenedor> buscarPorId(Long id) {
        return contenedorRepo.findById(id);
    }

    public Optional<Contenedor> buscarPorCodigo(String codigo) {
        return contenedorRepo.findByCodigoIdentificacion(codigo);
    }

    public Contenedor guardar(Contenedor nuevo) {
        if (contenedorRepo.existsByCodigoIdentificacion(nuevo.getCodigoIdentificacion())) {
            throw new RuntimeException("Ya existe un contenedor con ese código de identificación");
        }


        Cliente cliente = clienteRepo.findById(nuevo.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("El cliente indicado no existe"));

        nuevo.setCliente(cliente);
        return contenedorRepo.save(nuevo);
    }

    public Contenedor actualizar(Long id, Contenedor datos) {
        return contenedorRepo.findById(id)
                .map(c -> {
                    c.setCodigoIdentificacion(datos.getCodigoIdentificacion());
                    c.setPeso(datos.getPeso());
                    c.setVolumen(datos.getVolumen());
                    

                    if (datos.getCliente() != null && datos.getCliente().getId() != null) {
                        Cliente cliente = clienteRepo.findById(datos.getCliente().getId())
                                .orElseThrow(() -> new RuntimeException("El cliente indicado no existe"));
                        c.setCliente(cliente);
                    }
                    
                    return contenedorRepo.save(c);
                })
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));
    }

    public void eliminar(Long id) {
        if (!contenedorRepo.existsById(id)) {
            throw new RuntimeException("Contenedor no encontrado con ID: " + id);
        }
        contenedorRepo.deleteById(id);
    }

    public EstadoContenedorResponse obtenerEstado(Long id) {

        Contenedor contenedor = contenedorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));
        

        EstadoContenedorResponse.EstadoContenedorResponseBuilder builder = 
                EstadoContenedorResponse.builder()
                .idContenedor(contenedor.getId())
                .codigoIdentificacion(contenedor.getCodigoIdentificacion())
                .peso(contenedor.getPeso())
                .volumen(contenedor.getVolumen());
        

        if (contenedor.getCliente() != null) {
            builder.cliente(EstadoContenedorResponse.ClienteInfo.builder()
                    .id(contenedor.getCliente().getId())
                    .nombre(contenedor.getCliente().getNombre())
                    .apellido(contenedor.getCliente().getApellido())
                    .email(contenedor.getCliente().getEmail())
                    .build());
        }
        

        Optional<SolicitudLogisticaDTO> solicitudOpt = logisticaCliente.obtenerSolicitudActiva(id);
        
        if (solicitudOpt.isPresent()) {
            SolicitudLogisticaDTO solicitud = solicitudOpt.get();
            

            builder.solicitud(EstadoContenedorResponse.SolicitudInfo.builder()
                    .id(solicitud.getId())
                    .numeroSeguimiento(solicitud.getNumeroSeguimiento())
                    .estado(solicitud.getEstado())
                    .costoEstimado(solicitud.getCostoEstimado())
                    .costoFinal(solicitud.getCostoFinal())
                    .build());
            

            builder.ubicacionActual(solicitud.getUbicacionActual())
                   .descripcionUbicacion(solicitud.getDescripcionUbicacion());
            

            if (solicitud.getTramoActual() != null) {
                SolicitudLogisticaDTO.TramoActual tramoLogistica = solicitud.getTramoActual();
                builder.tramoActual(EstadoContenedorResponse.TramoInfo.builder()
                        .origen(tramoLogistica.getOrigen())
                        .destino(tramoLogistica.getDestino())
                        .estadoTramo(tramoLogistica.getEstadoTramo())
                        .patenteCamion(tramoLogistica.getPatenteCamion())
                        .build());
            }
        } else {

            builder.ubicacionActual("SIN_SOLICITUD")
                   .descripcionUbicacion("El contenedor no tiene una solicitud de transporte activa");
        }
        
        return builder.build();
    }

    public EstadoContenedorResponse obtenerEstadoPorCodigo(String codigo) {
        Contenedor contenedor = contenedorRepo.findByCodigoIdentificacion(codigo)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado con código: " + codigo));
        return obtenerEstado(contenedor.getId());
    }
}
