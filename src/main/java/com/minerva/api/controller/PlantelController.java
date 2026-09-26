package com.minerva.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.PlantelDTO;
import com.minerva.api.service.PlantelService;

@RestController 
@RequestMapping ("/api/planteles")
public class PlantelController {
    @Autowired 
    private PlantelService plantelService;
    
    @GetMapping 
    public ResponseEntity<List<PlantelDTO>> getlAllPlanteles(){
        return ResponseEntity.ok(plantelService.findAll());
    }

    @GetMapping("/{plantelId}")
    public ResponseEntity<PlantelDTO> getPlantelById(@PathVariable Long plantelId) {
        return ResponseEntity.ok(plantelService.getPlantelById(plantelId));
    }

    @PostMapping
    public ResponseEntity<PlantelDTO> savePlantel(@RequestBody PlantelDTO plantelDTO) {
        PlantelDTO creado = plantelService.savePlantel(plantelDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
}

    @PatchMapping("/{plantelId}")
    public ResponseEntity<PlantelDTO> updatePlantel(@PathVariable Long plantelId, @RequestBody PlantelDTO plantelDTO) {
        return ResponseEntity.ok(plantelService.updatePlantel(plantelId, plantelDTO));
    }

    @DeleteMapping("/{plantelId}")
    public ResponseEntity<Void> deletePlantel(@PathVariable Long plantelId) {
        plantelService.deletePlantel(plantelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{plantelId}/soft-delete")
    public ResponseEntity<?> softDeletePlantel(@PathVariable Long plantelId) {
        plantelService.softDeletePlantel(plantelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
