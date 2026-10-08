package com.minerva.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.service.MateriaService;

import org.springframework.web.bind.annotation.RequestBody;

@RestController 
@RequestMapping ("api/materias")
public class MateriaController {

    @Autowired 
    private MateriaService materiaService;

    @GetMapping 
    public ResponseEntity<List<MateriaDTO>> getAllMaterias(){
        return ResponseEntity.ok(materiaService.findAll());
    }

    @GetMapping("/{materiaId}")
    public ResponseEntity<MateriaDTO> getMateriaById(@PathVariable String materiaId){
        return ResponseEntity.ok(materiaService.getMateriaById(materiaId));
    }

    @PostMapping
    public ResponseEntity<MateriaDTO> saveMateria(@RequestBody MateriaDTO materiaDTO){
        MateriaDTO materiaCreada = materiaService.saveMateria(materiaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaCreada);
    }

    @PatchMapping("{materiaId}")
    public ResponseEntity<MateriaDTO> updateMateria(@PathVariable String materiaId, @RequestBody MateriaDTO materiaDTO){
        return ResponseEntity.ok(materiaService.updateMateria(materiaId, materiaDTO));
    }

    @PatchMapping("/{materiaId}/estado")
    public ResponseEntity<Void> softDeletePlantel(@PathVariable String materiaId, @RequestParam Boolean estado) {
        materiaService.updateEstado(materiaId, estado);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping ("/{materiaId}")
    public ResponseEntity<Void> deleteMateria(@PathVariable String materiaId){
        materiaService.deleteMateria(materiaId);
        return ResponseEntity.noContent().build();
    }
}
