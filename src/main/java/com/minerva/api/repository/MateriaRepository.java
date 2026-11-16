package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Materia;

public interface MateriaRepository extends JpaRepository<Materia, String> {
    
    boolean existsByNombreIgnoreCaseAndPlantelIdIsNull(String nombre);

    boolean existsByNombreIgnoreCaseAndPlantelId(String nombre, String plantelId);

    List<Materia> findAll();

    List<Materia>findByPlantelId(Long plantelId);

    List<Materia>findByPlantelIdIsNull();

    boolean existsByNombreIgnoreCaseAndPlantelIdIsNullAndIdNot(String nombre, String id);

    boolean existsByNombreIgnoreCase(String materia);

    boolean existsByNombreIgnoreCaseAndPlantelIdAndIdNot(String nombre, Long plantelId, String id);

    Optional<Materia> findByPlantelIdAndMateriaBaseId(Long plantelId, String materiaBaseId);
}
