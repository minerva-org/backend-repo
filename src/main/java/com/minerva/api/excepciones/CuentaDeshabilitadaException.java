package com.minerva.api.excepciones;

public class CuentaDeshabilitadaException extends RuntimeException {

    private final String codigo;

    public CuentaDeshabilitadaException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}