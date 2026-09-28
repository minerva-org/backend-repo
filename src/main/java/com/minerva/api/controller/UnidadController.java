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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.UnidadDTO;
import com.minerva.api.service.UnidadService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController 
@RequestMapping ("api/unidades")
public class UnidadController {
    private UnidadService unidadService;

    @GetMapping 
    public ResponseEntity<List<UnidadDTO>> getAllUnidades(){
        return ResponseEntity.ok(unidadService.findAll());
    }

    @GetMapping ("/{unidadId}")
    public ResponseEntity<UnidadDTO> getUnidadById(@PathVariable String unidadId){
        return ResponseEntity.ok(unidadService.getUnidadById(unidadId));
    }

    @GetMapping(params = "materiaId")
    public ResponseEntity<List<UnidadDTO>> getUnidadesByMateria(@RequestParam String materiaId) {
        return ResponseEntity.ok(unidadService.findAllByMateriaId(materiaId));
    }

    @PostMapping 
    public ResponseEntity<UnidadDTO> saveUnidad(@RequestBody UnidadDTO unidadDto){
        UnidadDTO unidadCreada = unidadService.saveUnidad(unidadDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(unidadCreada);
    }

    @PatchMapping ("/{unidadId}")
    public ResponseEntity<UnidadDTO> updateUnidad(@PathVariable String unidadId, @RequestBody UnidadDTO unidadDto){
        return ResponseEntity.ok(unidadService.updateUnidad(unidadId, unidadDto));
    }

    @DeleteMapping ("/{unidadId}")
    public ResponseEntity<Void> deleteUnidad(@PathVariable String unidadId){
        unidadService.deleteUnidad(unidadId);
        return ResponseEntity.noContent().build();
    }
    
}
