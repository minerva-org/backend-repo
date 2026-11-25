package com.minerva.api.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictoSolicitudException extends RuntimeException {
    public ConflictoSolicitudException(String mensaje) {
        super(mensaje);
    }
}
