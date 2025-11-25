package com.tpi.gestion.servicio;

import com.tpi.gestion.modelo.Cliente;
import com.tpi.gestion.repositorio.ClienteRepositorio;
import com.tpi.gestion.excepcion.RecursoNoEncontradoException;
import com.tpi.gestion.excepcion.DatosInvalidosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServicio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServicio.class);
    private final ClienteRepositorio repositorio;

    public ClienteServicio(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Cliente> listar() {
        return repositorio.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Cliente guardar(Cliente cliente) {
        if (repositorio.existsByEmail(cliente.getEmail())) {
            throw new DatosInvalidosException("Ya existe un cliente con ese correo electrónico");
        }
        return repositorio.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datos) {
        return repositorio.findById(id)
                .map(c -> {
                    c.setNombre(datos.getNombre());
                    c.setApellido(datos.getApellido());
                    c.setEmail(datos.getEmail());
                    c.setTelefono(datos.getTelefono());
                    return repositorio.save(c);
                })
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    @Transactional
    public void eliminar(Long id) {
        logger.info("Iniciando eliminación de cliente con ID: {}", id);
        
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("Cliente", id);
        }
        
        // Eliminar el cliente - Los contenedores asociados se manejan con la configuración de cascada o restricción de FK
        repositorio.deleteById(id);
        logger.info("Cliente eliminado exitosamente con ID: {}", id);
    }
}
