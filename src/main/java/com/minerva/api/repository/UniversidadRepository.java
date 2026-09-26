package com.minerva.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Universidad;

public interface UniversidadRepository extends JpaRepository<Universidad, Long>{
    
}
