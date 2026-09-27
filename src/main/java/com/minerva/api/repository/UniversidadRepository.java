package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.minerva.api.model.Universidad;

public interface UniversidadRepository extends JpaRepository<Universidad, Long>{
    List<Universidad> findAll();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
