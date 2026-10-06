package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.minerva.api.model.Institucion;

public interface InstitucionRepository extends JpaRepository<Institucion, Long>{
    List<Institucion> findAll();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
