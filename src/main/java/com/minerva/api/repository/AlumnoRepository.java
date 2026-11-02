package com.minerva.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Alumno;

public interface AlumnoRepository extends JpaRepository <Alumno, String>{
    
}
