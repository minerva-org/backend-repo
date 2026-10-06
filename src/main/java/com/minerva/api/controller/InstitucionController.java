package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.InstitucionDTO;
import com.minerva.api.service.InstitucionService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/instituciones")
@RequiredArgsConstructor
public class InstitucionController {
    private final InstitucionService institucionService;

    @GetMapping
    public ResponseEntity<List<InstitucionDTO>> getAllInstituciones(){
        return ResponseEntity.ok(institucionService.findAll());
    }

    @GetMapping ("/{institucionId}")
    public ResponseEntity<InstitucionDTO> getInstitucionById(@PathVariable Long institucionId){
        return ResponseEntity.ok(institucionService.getInstitucionById(institucionId));
    }

    @PostMapping 
    public ResponseEntity<InstitucionDTO> saveInstitucion(@RequestBody InstitucionDTO institucionDTO){
        InstitucionDTO institucionCreada = institucionService.saveInstitucion(institucionDTO);
        return ResponseEntity.status(HttpStatus.CREATED). body(institucionCreada);
    }

    @PatchMapping ("/{institucionId}")
    public ResponseEntity<InstitucionDTO> updateInstitucion(@PathVariable Long institucionId, @RequestBody InstitucionDTO institucionDTO ){
        return ResponseEntity.ok(institucionService.updateInstitucion(institucionId, institucionDTO));
    } 

    @DeleteMapping ("/{institucionId}")
    public ResponseEntity<Void> deleteInstitucion(@PathVariable Long institucionId){
        institucionService.deleteInstitucion(institucionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{institucionId}/soft-delete")
    public ResponseEntity<Void> softDeleteInstitucion(@PathVariable Long institucionId) {
        institucionService.softDeleteInstitucion(institucionId);
        return ResponseEntity.noContent().build();
    }
}
