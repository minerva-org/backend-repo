package com.minerva.api.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.minerva.api.excepciones.IntentoFinalizadoEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumenQuizListener {

    private final ResumenQuizService resumenQuizService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alFinalizarIntento(IntentoFinalizadoEvent evento) {
        try {
            resumenQuizService.recalcular(evento.quizId());
        } catch (Exception e) {
            log.warn("No se pudo actualizar el resumen del quiz {}", evento.quizId(), e);
        }
    }
}