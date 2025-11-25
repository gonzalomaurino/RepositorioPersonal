package com.tpi.gestion.excepcion;

/**
 * Excepción lanzada cuando se reciben datos inválidos.
 */
public class DatosInvalidosException extends RuntimeException {
    
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
    
    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
