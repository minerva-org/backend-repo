package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.UniversidadDTO;
import com.minerva.api.service.UniversidadService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController 
@RequestMapping ("api/universidades")
public class UniversidadController {
    private UniversidadService universidadService;

    @GetMapping
    public ResponseEntity<List<UniversidadDTO>> getAllUniversidades(){
        return ResponseEntity.ok(universidadService.findAll());
    }

    @GetMapping ("/{universidadId}")
    public ResponseEntity<UniversidadDTO> getUniversidadById(@PathVariable Long universidadId){
        return ResponseEntity.ok(universidadService.getUniversidadById(universidadId));
    }

    @PostMapping 
    public ResponseEntity<UniversidadDTO> saveUniversidad(@RequestBody UniversidadDTO universidadDTO){
        UniversidadDTO universidadCreada = universidadService.saveUniversidad(universidadDTO);
        return ResponseEntity.status(HttpStatus.CREATED). body(universidadCreada);
    }

    @PatchMapping ("/{universidadId}")
    public ResponseEntity<UniversidadDTO> updateUniversidad(@PathVariable Long universidadId, @RequestBody UniversidadDTO universidadDTO ){
        return ResponseEntity.ok(universidadService.updateUniversidad(universidadId, universidadDTO));
    } 

    @DeleteMapping ("/{universidadId}")
    public ResponseEntity<Void> deleteUniversidad(@PathVariable Long universidadId){
        universidadService.deleteUniversidad(universidadId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{universidadId}/soft-delete")
    public ResponseEntity<Void> softDeleteUniversidad(@PathVariable Long universidadId) {
        universidadService.softDeleteUniversidad(universidadId);
        return ResponseEntity.noContent().build();
    }
}
