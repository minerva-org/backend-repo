package com.minerva.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Grupo;

public interface GrupoRepository extends JpaRepository<Grupo, String> {
    
}
