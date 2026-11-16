package com.minerva.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Periodo;

public interface PeriodoRepository extends JpaRepository<Periodo, String> {
    Periodo findFirstByOrderByFechaCreacionDesc();
}
