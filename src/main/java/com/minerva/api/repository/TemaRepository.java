package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.minerva.api.model.Tema;

public interface TemaRepository extends JpaRepository<Tema, String> {

    List<Tema> findByUnidadId(String unidadId);

    boolean existsByNombreIgnoreCaseAndUnidadId(String nombre, String unidadId);

    boolean existsByNombreIgnoreCaseAndUnidadIdAndIdNot(String nombre, String unidadId, String id);
}