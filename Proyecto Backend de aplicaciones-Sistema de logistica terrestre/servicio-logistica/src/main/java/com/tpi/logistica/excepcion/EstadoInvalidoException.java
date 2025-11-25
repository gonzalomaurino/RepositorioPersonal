package com.tpi.logistica.excepcion;

/**
 * Excepción lanzada cuando se intenta realizar una operación con un estado inválido.
 */
public class EstadoInvalidoException extends RuntimeException {
    
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
    
    public EstadoInvalidoException(String entidad, String estadoActual, String estadoEsperado) {
        super(String.format("%s en estado '%s', se esperaba '%s'", entidad, estadoActual, estadoEsperado));
    }
}
