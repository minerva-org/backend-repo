package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minerva.api.model.Unidad;

public interface UnidadRepository extends JpaRepository<Unidad, String>{
    boolean existsByNombreIgnoreCase(String nombre);

    List<Unidad> findAll();

    List<Unidad> findByMateriaId(String materiaId);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, String id);

    boolean existsByNombreIgnoreCaseAndMateriaId(String nombre, String materiaId);
    
    boolean existsByNombreIgnoreCaseAndMateriaIdAndIdNot(String nombre, String materiaId, String id);

    @Modifying
    @Query(value = """
        UPDATE unidad
        SET activo = :activo
        WHERE id_materia = :idMateria
        """, nativeQuery = true)
    void cambiarEstadoPorMateria(@Param("idMateria") String idMateria, @Param("activo") boolean activo);
}
