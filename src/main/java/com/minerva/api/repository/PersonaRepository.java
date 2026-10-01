package com.minerva.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.minerva.api.model.Persona;
import com.minerva.api.User.Roles;

public interface PersonaRepository extends JpaRepository<Persona, String> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, String id);

    List<Persona> findByPlantelId(Long plantelId);
    
    List<Persona> findByRol(Roles rol);

    List<Persona> findByPlantelIdAndRol(Long plantelId, Roles rol);
}