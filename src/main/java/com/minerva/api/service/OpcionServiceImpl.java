package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.OpcionDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Opcion;
import com.minerva.api.model.Pregunta;
import com.minerva.api.repository.OpcionRepository;
import com.minerva.api.repository.PreguntaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpcionServiceImpl implements OpcionService {

    private final OpcionRepository opcionRepository;
    private final PreguntaRepository preguntaRepository;

    @Override
    @Transactional
    public OpcionDTO saveOpcion(OpcionDTO opcionDTO) {
        if (opcionDTO.getId() == null || opcionDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = opcionDTO.getId().trim();
        if (opcionRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe una opción con ese Id: " + id);
        }

        if (opcionDTO.getDescripcion() == null || opcionDTO.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        String descripcion = opcionDTO.getDescripcion().trim();

        if (opcionDTO.getPreguntaId() == null || opcionDTO.getPreguntaId().isBlank()) {
            throw new IllegalArgumentException("La pregunta es obligatoria");
        }
        String preguntaId = opcionDTO.getPreguntaId().trim();
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId));

        boolean esCorrecta = Boolean.TRUE.equals(opcionDTO.getEsCorrecta());
        if (esCorrecta && opcionRepository.existsByPreguntaIdAndEsCorrectaTrue(preguntaId)) {
            throw new IllegalArgumentException(
                "Esta pregunta ya tiene una opción marcada como correcta");
        }

        Opcion opcion = new Opcion();
        opcion.setId(id);
        opcion.setDescripcion(descripcion);
        opcion.setEsCorrecta(esCorrecta);
        opcion.setPregunta(pregunta);

        return Mapper.toDTO(opcionRepository.save(opcion));
    }

    @Override
    @Transactional
    public OpcionDTO updateOpcion(String opcionId, OpcionDTO opcionDTO) {
        Opcion opcion = opcionRepository.findById(opcionId)
            .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada con el id: " + opcionId));

        if (opcionDTO.getDescripcion() != null) {
            String descripcion = opcionDTO.getDescripcion().trim();
            if (descripcion.isEmpty()) {
                throw new IllegalArgumentException("La descripción no puede estar vacía");
            }
            opcion.setDescripcion(descripcion);
        }

        if (opcionDTO.getPreguntaId() != null) {
            String preguntaId = opcionDTO.getPreguntaId().trim();
            Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No existe una pregunta con el id: " + preguntaId));
            opcion.setPregunta(pregunta);
        }

        if (opcionDTO.getEsCorrecta() != null) {
            boolean esCorrecta = opcionDTO.getEsCorrecta();
            String preguntaId = opcion.getPregunta().getId();

            if (esCorrecta) {
                if (opcionRepository.existsByPreguntaIdAndEsCorrectaTrueAndIdNot(preguntaId, opcionId)) {
                    throw new IllegalArgumentException(
                        "Esta pregunta ya tiene otra opción marcada como correcta");
                }
            } else if (Boolean.TRUE.equals(opcion.getEsCorrecta())) {
                boolean quedaAlgunaOtraCorrecta = opcionRepository
                    .existsByPreguntaIdAndEsCorrectaTrueAndIdNot(preguntaId, opcionId);
                if (!quedaAlgunaOtraCorrecta) {
                    throw new IllegalArgumentException(
                        "La pregunta debe conservar al menos una opción correcta");
                }
            }
            opcion.setEsCorrecta(esCorrecta);
        }

        return Mapper.toDTO(opcionRepository.save(opcion));
    }

    @Override
    @Transactional
    public void deleteOpcion(String opcionId) {
        Opcion opcion = opcionRepository.findById(opcionId)
            .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada con el id: " + opcionId));

        if (Boolean.TRUE.equals(opcion.getEsCorrecta())) {
            throw new IllegalArgumentException(
                "No se puede eliminar la opción correcta de una pregunta. Marca otra opción como correcta primero.");
        }

        opcionRepository.delete(opcion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionDTO> findAll() {
        return opcionRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionDTO getOpcionById(String opcionId) {
        Opcion opcion = opcionRepository.findById(opcionId)
            .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada con el id: " + opcionId));

        return Mapper.toDTO(opcion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionDTO> findAllByPreguntaId(String preguntaId) {
        if (!preguntaRepository.existsById(preguntaId)) {
            throw new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId);
        }
        return opcionRepository.findByPreguntaId(preguntaId)
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }
}