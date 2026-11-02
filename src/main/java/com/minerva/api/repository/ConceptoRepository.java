package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minerva.api.model.Concepto;

public interface ConceptoRepository extends JpaRepository<Concepto, String> {
    
    List<Concepto> findByTemaId(String temaId);

    boolean existsByNombreIgnoreCaseAndTemaId(String nombre, String conceptoId);

    boolean existsByNombreIgnoreCaseAndTemaIdAndIdNot(String nombre, String temaId, String id);
    
    @Modifying
    @Query(value = """
        UPDATE concepto
        SET activo = :activo
        WHERE id_tema IN (
            SELECT t.id FROM tema t
            JOIN unidad u ON t.id_unidad = u.id
            WHERE u.id_materia = :idMateria)
        """, nativeQuery = true)
    void cambiarEstadoPorMateria(@Param("idMateria") String idMateria, @Param("activo") boolean activo);
}
