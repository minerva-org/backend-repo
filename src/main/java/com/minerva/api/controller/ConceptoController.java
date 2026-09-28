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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.ConceptoDTO;
import com.minerva.api.service.ConceptoService;

@RestController 
@RequestMapping ("api/conceptos")
public class ConceptoController {

    @Autowired 
    private ConceptoService conceptoService;

    @GetMapping
    public ResponseEntity<List<ConceptoDTO>> getAllConceptos() {
        return ResponseEntity.ok(conceptoService.findAll());
    }

    @GetMapping(params = "conceptoId")
    public ResponseEntity<List<ConceptoDTO>> getConceptosByTemas(@RequestParam String temaId) {
        return ResponseEntity.ok(conceptoService.findAllByTemaId(temaId));
    }

    @GetMapping("/{conceptoId}")
    public ResponseEntity<ConceptoDTO> getTemaById(@PathVariable String temaId) {
        return ResponseEntity.ok(conceptoService.getConceptoById(temaId));
    }

    @PostMapping
    public ResponseEntity<ConceptoDTO> saveConcepto(@RequestBody ConceptoDTO conceptoDTO) {
        ConceptoDTO conceptoCreado = conceptoService.saveConcepto(conceptoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoCreado);
    }

    @PatchMapping("/{conceptoId}")
    public ResponseEntity<ConceptoDTO> updateConcepto(
            @PathVariable String conceptoId, @RequestBody ConceptoDTO conceptoDTO) {
        return ResponseEntity.ok(conceptoService.updateConcepto(conceptoId, conceptoDTO));
    }

    @DeleteMapping("/{conceptoId}")
    public ResponseEntity<Void> deleteTema(@PathVariable String conceptoId) {
        conceptoService.deleteConcepto(conceptoId);
        return ResponseEntity.noContent().build();
    }
}
