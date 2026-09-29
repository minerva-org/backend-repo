package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Concepto;

public interface ConceptoRepository extends JpaRepository<Concepto, String> {
    List<Concepto> findByTemaId(String temaId);

    boolean existsByNombreIgnoreCaseAndTemaId(String nombre, String conceptoId);

    boolean existsByNombreIgnoreCaseAndTemaIdAndIdNot(String nombre, String temaId, String id);
    
}
