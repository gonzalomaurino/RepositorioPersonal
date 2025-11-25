package com.tpi.gestion.servicio;

import com.tpi.gestion.modelo.Deposito;
import com.tpi.gestion.repositorio.DepositoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepositoServicio {

    private final DepositoRepositorio repositorio;


    public DepositoServicio(DepositoRepositorio repositorio) {
        this.repositorio = repositorio;
    }


    public List<Deposito> listar() {
        return repositorio.findAll();
    }


    public Optional<Deposito> buscarPorId(Long id) {
        return repositorio.findById(id);
    }


    public Deposito guardar(Deposito nuevoDeposito) {
        return repositorio.save(nuevoDeposito);
    }


    public Deposito actualizar(Long id, Deposito datosActualizados) {
        return repositorio.findById(id)
                .map(deposito -> {
                    deposito.setNombre(datosActualizados.getNombre());
                    deposito.setDireccion(datosActualizados.getDireccion());
                    deposito.setLatitud(datosActualizados.getLatitud());
                    deposito.setLongitud(datosActualizados.getLongitud());
                    deposito.setCostoEstadiaXdia(datosActualizados.getCostoEstadiaXdia());
                    return repositorio.save(deposito);
                })
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado"));
    }


    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RuntimeException("Depósito no encontrado con ID: " + id);
        }
        repositorio.deleteById(id);
    }
}
