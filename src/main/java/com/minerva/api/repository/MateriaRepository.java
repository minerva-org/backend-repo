package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Materia;

public interface MateriaRepository extends JpaRepository<Materia, String> {
    
    boolean existsByNombreIgnoreCase(String nombre);

    List<Materia> findAll();

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, String id);
}
