package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Configuracion;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {
    List<Configuracion> findAll();

    Configuracion findByValor(String valor);
}
