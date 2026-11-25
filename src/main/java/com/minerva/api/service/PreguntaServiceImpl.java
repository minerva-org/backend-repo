package com.minerva.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Tema;
import com.minerva.api.model.Unidad;
import com.minerva.api.model.Opcion;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.OpcionRepository;
import com.minerva.api.repository.PreguntaRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.minerva.api.model.ConceptoGrupo;
import com.minerva.api.repository.ConceptoGrupoRepository;
import com.minerva.api.repository.TemaGrupoRepository;
import com.minerva.api.repository.UnidadGrupoRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadRepository;

@Service
@RequiredArgsConstructor
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final ConceptoRepository conceptoRepository;
    private final OpcionRepository opcionRepository;
    private final QuizXPreguntaRepository quizXPreguntaRepository;
    private final ConceptoGrupoRepository conceptoGrupoRepository;
    private final TemaGrupoRepository temaGrupoRepository;
    private final UnidadGrupoRepository unidadGrupoRepository;
    private final TemaRepository temaRepository;
    private final UnidadRepository unidadRepository;

    private Concepto resolverConcepto(String conceptoId, String grupoId) {
        if (conceptoId == null || conceptoId.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }

        ConceptoGrupo conceptoGrupo =
                conceptoGrupoRepository.findById(conceptoId).orElse(null);
        if (conceptoGrupo != null) {
            if (grupoId == null || grupoId.isBlank()) {
                throw new IllegalArgumentException(
                        "Se requiere el grupo para resolver un concepto de currículo.");
            }
            String grupoConcepto = conceptoGrupo.getTemaGrupo()
                    .getUnidadGrupo()
                    .getGrupo()
                    .getId();
            if (!grupoId.equals(grupoConcepto)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El concepto no pertenece al grupo del quiz.");
            }
            if (conceptoGrupo.getConceptoOrigen() == null) {
                Concepto materializado =
                        materializarConceptoBase(conceptoGrupo);
                conceptoGrupo.setConceptoOrigen(materializado);
                conceptoGrupoRepository.save(conceptoGrupo);
            }
            return conceptoGrupo.getConceptoOrigen();
        }

        Concepto concepto = conceptoRepository.findById(conceptoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Concepto no encontrado con el id: " + conceptoId));
        if (grupoId != null && !grupoId.isBlank()
                && !conceptoGrupoRepository
                        .existsByConceptoOrigenIdAndTemaGrupoUnidadGrupoGrupoId(
                                conceptoId,
                                grupoId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El concepto no pertenece al currículo del grupo.");
        }
        return concepto;
    }

    private Concepto materializarConceptoBase(
            ConceptoGrupo conceptoGrupo) {
        var temaGrupo = conceptoGrupo.getTemaGrupo();
        Tema temaBase = temaGrupo.getTemaOrigen();
        if (temaBase == null) {
            var unidadGrupo = temaGrupo.getUnidadGrupo();
            Unidad unidadBase = unidadGrupo.getUnidadOrigen();
            if (unidadBase == null) {
                var materia = unidadGrupo.getMateria() != null
                        ? unidadGrupo.getMateria()
                        : unidadGrupo.getGrupo().getMateria();
                if (materia == null) {
                    throw new EntityNotFoundException(
                            "La unidad del grupo no tiene materia base asociada.");
                }
                unidadBase = unidadRepository
                        .findFirstByNombreIgnoreCaseAndMateriaId(
                                unidadGrupo.getNombre(),
                                materia.getId())
                        .orElseGet(() -> {
                            Unidad unidad = new Unidad();
                            unidad.setId(UUID.randomUUID().toString());
                            unidad.setNombre(unidadGrupo.getNombre());
                            unidad.setMateria(materia);
                            unidad.setActivo(true);
                            return unidadRepository.save(unidad);
                        });
                unidadGrupo.setUnidadOrigen(unidadBase);
                unidadGrupoRepository.save(unidadGrupo);
            }

            final Unidad resolvedUnidadBase = unidadBase;
            temaBase = temaRepository
                    .findFirstByNombreIgnoreCaseAndUnidadId(
                            temaGrupo.getNombre(),
                            resolvedUnidadBase.getId())
                    .orElseGet(() -> {
                        Tema tema = new Tema();
                        tema.setId(UUID.randomUUID().toString());
                        tema.setNombre(temaGrupo.getNombre());
                        tema.setUnidad(resolvedUnidadBase);
                        tema.setActivo(true);
                        return temaRepository.save(tema);
                    });
            temaGrupo.setTemaOrigen(temaBase);
            temaGrupoRepository.save(temaGrupo);
        }

        final Tema resolvedTemaBase = temaBase;
        return conceptoRepository
                .findFirstByNombreIgnoreCaseAndTemaId(
                        conceptoGrupo.getNombre(),
                        resolvedTemaBase.getId())
                .orElseGet(() -> {
                    Concepto concepto = new Concepto();
                    concepto.setId(UUID.randomUUID().toString());
                    concepto.setNombre(conceptoGrupo.getNombre());
                    concepto.setTema(resolvedTemaBase);
                    concepto.setActivo(true);
                    return conceptoRepository.save(concepto);
                });
    }

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
        Concepto concepto = resolverConcepto(conceptoId, preguntaDTO.getGrupoId());

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
            Concepto concepto = resolverConcepto(conceptoId, preguntaDTO.getGrupoId());
            pregunta.setConcepto(concepto);
        }

        return Mapper.toDTO(preguntaRepository.save(pregunta));
    }

    @Override
    @Transactional
    public void deletePregunta(String preguntaId) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId));

        List<Opcion> opciones = opcionRepository.findByPreguntaId(preguntaId);
        if (!opciones.isEmpty()) {
            opcionRepository.deleteAll(opciones);
        }

        List<QuizXPregunta> relacionesQuiz = quizXPreguntaRepository.findByPreguntaId(preguntaId);
        if (!relacionesQuiz.isEmpty()) {
            quizXPreguntaRepository.deleteAll(relacionesQuiz);
        }

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