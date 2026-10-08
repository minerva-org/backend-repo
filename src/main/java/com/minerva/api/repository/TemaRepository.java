package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minerva.api.model.Tema;

public interface TemaRepository extends JpaRepository<Tema, String> {

    List<Tema> findByUnidadId(String unidadId);

    boolean existsByNombreIgnoreCaseAndUnidadId(String nombre, String unidadId);

    boolean existsByNombreIgnoreCaseAndUnidadIdAndIdNot(String nombre, String unidadId, String id);

    @Modifying
    @Query(value = """
        UPDATE tema
        SET activo = :activo
        WHERE id_unidad IN (SELECT id FROM unidad WHERE id_materia = :idMateria)
        """, nativeQuery = true)
    void cambiarEstadoPorMateria(@Param("idMateria") String idMateria, @Param("activo") boolean activo);
}