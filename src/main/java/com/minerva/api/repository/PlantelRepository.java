package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Plantel;

public interface PlantelRepository extends JpaRepository<Plantel, Long>{
    List<Plantel> findAll();
    
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Plantel> findByUniversidadIdAndActivoTrue(Long universidadId);
    
}
