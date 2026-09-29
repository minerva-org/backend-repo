package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Pregunta;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.PreguntaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final ConceptoRepository conceptoRepository;

    @Override
    @Transactional
    public PreguntaDTO savePregunta(PreguntaDTO preguntaDTO) {
        if (preguntaDTO.getId() == null || preguntaDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = preguntaDTO.getId().trim();
        if (preguntaRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe una pregunta con ese Id: " + id);
        }

        if (preguntaDTO.getDescripcion() == null || preguntaDTO.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        String descripcion = preguntaDTO.getDescripcion().trim();

        if (preguntaDTO.getConceptoId() == null || preguntaDTO.getConceptoId().isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
        String conceptoId = preguntaDTO.getConceptoId().trim();
        Concepto concepto = conceptoRepository.findById(conceptoId)
            .orElseThrow(() -> new EntityNotFoundException("Concepto no encontrado con el id: " + conceptoId));

        Pregunta pregunta = new Pregunta();
        pregunta.setId(id);
        pregunta.setDescripcion(descripcion);
        pregunta.setConcepto(concepto);

        return Mapper.toDTO(preguntaRepository.save(pregunta));
    }

    @Override
    @Transactional
    public PreguntaDTO updatePregunta(String preguntaId, PreguntaDTO preguntaDTO) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId));

        if (preguntaDTO.getDescripcion() != null) {
            String descripcion = preguntaDTO.getDescripcion().trim();
            if (descripcion.isEmpty()) {
                throw new IllegalArgumentException("La descripción no puede estar vacía");
            }
            pregunta.setDescripcion(descripcion);
        }

        if (preguntaDTO.getConceptoId() != null) {
            String conceptoId = preguntaDTO.getConceptoId().trim();
            Concepto concepto = conceptoRepository.findById(conceptoId)
                .orElseThrow(() -> new EntityNotFoundException("No existe un concepto con el id: " + conceptoId));
            pregunta.setConcepto(concepto);
        }

        return Mapper.toDTO(preguntaRepository.save(pregunta));
    }

    @Override
    @Transactional
    public void deletePregunta(String preguntaId) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId));

        preguntaRepository.delete(pregunta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreguntaDTO> findAll() {
        return preguntaRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PreguntaDTO getPreguntaById(String preguntaId) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId));

        return Mapper.toDTO(pregunta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreguntaDTO> findAllByConceptoId(String conceptoId) {
        if (!conceptoRepository.existsById(conceptoId)) {
            throw new EntityNotFoundException("Concepto no encontrado con el id: " + conceptoId);
        }
        return preguntaRepository.findByConceptoId(conceptoId)
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }
}