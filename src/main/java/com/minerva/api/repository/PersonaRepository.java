package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.User.Roles;

public interface PersonaRepository extends JpaRepository<Persona, String> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, String id);

    List<Persona> findByPlantelId(Long plantelId);

    List<Persona> findByRol(Roles rol);

    List<Persona> findByPlantelIdAndRol(Long plantelId, Roles rol);

    List<Persona> findByPlantelIdAndRolAndActivo(Long plantelId, Roles rol, Boolean status);

    // los siguientes dos son una prueba para el paginado
    Page<Persona> findByPlantelIdAndRol(long plantelId, Roles rol, Pageable pageable);

    Page<Persona> findByPlantelIdAndRolAndActivo(long plantelId, Roles rol, Boolean status, Pageable pageable);

    Plantel findByPlantelIdAndId(Long plantelId, String personaId);
}