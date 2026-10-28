package com.minerva.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.minerva.api.model.Docente;

public interface DocenteRepository extends JpaRepository <Docente, String> {
    
}
