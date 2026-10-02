package com.minerva.api.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.minerva.api.dto.PersonaDTO;
import com.minerva.api.User.Roles;
import com.minerva.api.service.PersonaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    @GetMapping
    public ResponseEntity<List<PersonaDTO>> getAll() {
        return ResponseEntity.ok(personaService.findAll());
    }

    @GetMapping(params = "plantelId")
    public ResponseEntity<List<PersonaDTO>> getByPlantel(@RequestParam Long plantelId) {
        return ResponseEntity.ok(personaService.findAllByPlantelId(plantelId));
    }

    @GetMapping(params = {"plantelId", "rol=DOCENTE"})
    public ResponseEntity<List<PersonaDTO>> getDocentesByPlantel(@RequestParam Long plantelId) {
        return ResponseEntity.ok(personaService.findAllDocentesByPlantelId(plantelId));
    }

    @GetMapping(params = {"plantelId", "rol=ALUMNO"})
    public ResponseEntity<List<PersonaDTO>> getAlumnosByPlantel(@RequestParam Long plantelId) {
        return ResponseEntity.ok(personaService.findAllByPlantelIdAndRol(plantelId, Roles.ALUMNO));
    }

    @GetMapping(params = {"plantelId", "rol=ALUMNO","activo"})
    public ResponseEntity<List<PersonaDTO>> getAlumnosActivosByPlantel(@RequestParam Long plantelId, @RequestParam boolean activo) {
        return ResponseEntity.ok(personaService.findAllByPlantelIdAndRolAndStatus(plantelId, Roles.ALUMNO, activo));
    }

    @GetMapping(params = "rol")
    public ResponseEntity<List<PersonaDTO>> getByRol(@RequestParam Roles rol) {
        return ResponseEntity.ok(personaService.findAllByRol(rol));
    }

    @GetMapping("/{personaId}")
    public ResponseEntity<PersonaDTO> getById(@PathVariable String personaId) {
        return ResponseEntity.ok(personaService.getPersonaById(personaId));
    }

    @PostMapping
    public ResponseEntity<PersonaDTO> save(@RequestBody PersonaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personaService.savePersona(dto));
    }

    @PatchMapping("/{personaId}")
    public ResponseEntity<PersonaDTO> update(@PathVariable String personaId, @RequestBody PersonaDTO dto) {
        return ResponseEntity.ok(personaService.updatePersona(personaId, dto));
    }

    @DeleteMapping("/{personaId}")
    public ResponseEntity<Void> delete(@PathVariable String personaId) {
        personaService.deletePersona(personaId);
        return ResponseEntity.noContent().build();
    }
}