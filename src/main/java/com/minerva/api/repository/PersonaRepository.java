package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT p FROM Persona p
            WHERE p.plantel.id = :plantelId
            And p.rol = :rol
            AND (:activo IS NULL OR p.activo = :activo)
            AND (
            LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.apellidoMaterno) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%'))
            ) """)

    Page<Persona> findPaginatedPeople(@Param("plantelId") Long plantelId, @Param("rol") Roles rol,
            @Param("activo") Boolean activo, @Param("search") String search, Pageable pageable);

    Plantel findByPlantelIdAndId(Long plantelId, String personaId);
}