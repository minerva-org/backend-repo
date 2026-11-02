package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.IntentoDetalle;

public interface IntentoDetalleRepository extends JpaRepository<IntentoDetalle, UUID> {
    Optional<IntentoDetalle> findByIntentoIdAndPreguntaId(String intentoId, String preguntaId);
    
    long countByIntentoIdAndEsCorrectoTrue(String intentoId);

    List<IntentoDetalle> findByIntentoId(String intentoId);
    
    long countByIntentoId(String intentoId);

    long countByIntentoIdAndOpcionSeleccionadaIsNull(String intentoId);
}