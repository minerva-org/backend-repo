package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minerva.api.model.Grupo;

public interface GrupoRepository extends JpaRepository<Grupo, String> {

    boolean existsByClaveGrupoIgnoreCase(String claveGrupo);

    boolean existsByClaveGrupoIgnoreCaseAndIdNot(String claveGrupo, String id);

    List<Grupo> findByDocenteId(String docenteId);

    List<Grupo> findByPlantelId(Long plantelId);

    List<Grupo> findByAlumnos_Id(String alumnoId);

    @Query("SELECT g FROM Grupo g WHERE g.id = :identifier OR LOWER(g.claveGrupo) = LOWER(:identifier)")
    Optional<Grupo> findByIdOrClaveGrupo(@Param("identifier") String identifier);
}