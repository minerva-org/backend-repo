package com.minerva.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.ConceptoDTO;
import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.dto.TemaDTO;
import com.minerva.api.dto.UnidadDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Materia;
import com.minerva.api.model.PlanEstudio;
import com.minerva.api.model.Tema;
import com.minerva.api.model.Unidad;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.MateriaRepository;
import com.minerva.api.repository.PlanEstudioRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MateriaServiceImpl implements MateriaService{

    private final MateriaRepository materiaRepository;
    private final PlanEstudioRepository planEstudioRepository;
    private final UnidadRepository unidadRepository;
    private final TemaRepository temaRepository;
    private final ConceptoRepository conceptoRepository;

    private MateriaDTO toMateriaDetailDto(Materia materia) {
        MateriaDTO dto = Mapper.toDTO(materia);
        if (dto == null) {
            return null;
        }

        List<UnidadDTO> unidades = unidadRepository.findByMateriaId(materia.getId()).stream()
            .map(unidad -> {
                UnidadDTO unidadDto = Mapper.toDTO(unidad);
                unidadDto.setTemas(temaRepository.findByUnidadId(unidad.getId()).stream()
                    .map(tema -> {
                        TemaDTO temaDto = Mapper.toDTO(tema);
                        temaDto.setConceptos(conceptoRepository.findByTemaId(tema.getId()).stream()
                            .map(Mapper::toDTO)
                            .toList());
                        return temaDto;
                    })
                    .toList());
                return unidadDto;
            })
            .toList();

        dto.setUnidades(unidades);
        return dto;
    }

    private void persistNestedHierarchy(Materia materia, List<UnidadDTO> unidades) {
        if (unidades == null || unidades.isEmpty()) {
            return;
        }

        for (UnidadDTO unidadDTO : unidades) {
            if (unidadDTO == null) {
                continue;
            }
            String nombreUnidad = unidadDTO.getNombre() == null ? null : unidadDTO.getNombre().trim();
            if (nombreUnidad == null || nombreUnidad.isBlank()) {
                continue;
            }

            Unidad unidad = new Unidad();
            unidad.setId(unidadDTO.getId() == null || unidadDTO.getId().isBlank() ? java.util.UUID.randomUUID().toString() : unidadDTO.getId().trim());
            unidad.setNombre(nombreUnidad);
            unidad.setMateria(materia);
            Unidad unidadGuardada = unidadRepository.save(unidad);

            if (unidadDTO.getTemas() == null || unidadDTO.getTemas().isEmpty()) {
                continue;
            }

            for (TemaDTO temaDTO : unidadDTO.getTemas()) {
                if (temaDTO == null) {
                    continue;
                }
                String nombreTema = temaDTO.getNombre() == null ? null : temaDTO.getNombre().trim();
                if (nombreTema == null || nombreTema.isBlank()) {
                    continue;
                }

                Tema tema = new Tema();
                tema.setId(temaDTO.getId() == null || temaDTO.getId().isBlank() ? java.util.UUID.randomUUID().toString() : temaDTO.getId().trim());
                tema.setNombre(nombreTema);
                tema.setUnidad(unidadGuardada);
                Tema temaGuardado = temaRepository.save(tema);

                if (temaDTO.getConceptos() == null || temaDTO.getConceptos().isEmpty()) {
                    continue;
                }

                for (ConceptoDTO conceptoDTO : temaDTO.getConceptos()) {
                    if (conceptoDTO == null) {
                        continue;
                    }
                    String nombreConcepto = conceptoDTO.getNombre() == null ? null : conceptoDTO.getNombre().trim();
                    if (nombreConcepto == null || nombreConcepto.isBlank()) {
                        continue;
                    }

                    Concepto concepto = new Concepto();
                    concepto.setId(conceptoDTO.getId() == null || conceptoDTO.getId().isBlank() ? java.util.UUID.randomUUID().toString() : conceptoDTO.getId().trim());
                    concepto.setNombre(nombreConcepto);
                    concepto.setTema(temaGuardado);
                    conceptoRepository.save(concepto);
                }
            }
        }
    }

    @Override
    @Transactional 
    public MateriaDTO saveMateria(MateriaDTO materiaDTO) {
        if (materiaDTO.getId() == null) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        if (materiaRepository.existsById(materiaDTO.getId())) {
            throw new IllegalArgumentException("Ya existe una materia con ese Id: " + materiaDTO.getId());
        }

        if (materiaDTO.getNombre() == null || materiaDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String nombre = materiaDTO.getNombre().trim();
        if (materiaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una materia con ese nombre");
        }

        String planEstudioId = materiaDTO.getPlanEstudioId();
        PlanEstudio planEstudio = null;
        if (planEstudioId != null && !planEstudioId.isBlank()) {
            planEstudio = planEstudioRepository.findById(planEstudioId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Plan de estudio no encontrado con el Id: " + planEstudioId));
        }

        Materia materia = new Materia();
        materia.setId(materiaDTO.getId());
        materia.setNombre(nombre);
        materia.setPrefijo(materiaDTO.getPrefijo());
        materia.setPlanEstudio(planEstudio);

        Materia materiaGuardada = materiaRepository.save(materia);
        persistNestedHierarchy(materiaGuardada, materiaDTO.getUnidades());
        return toMateriaDetailDto(materiaGuardada);
    }

    @Override
    @Transactional
    public MateriaDTO updateMateria(String materiaId, MateriaDTO materiaDTO) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Materia no encontrada con el Id: " + materiaId));

        if (materiaDTO.getNombre() != null) {
            String nombre = materiaDTO.getNombre().trim();
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            if (materiaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, materiaId)) {
                throw new IllegalArgumentException("Ya existe una materia con ese nombre");
            }
            materia.setNombre(nombre);
        }

        if (materiaDTO.getPrefijo() != null) {
            String prefijo = materiaDTO.getPrefijo().trim();
            if (prefijo.isEmpty()) {
                throw new IllegalArgumentException("El prefijo no puede estar vacío");
            }
            materia.setPrefijo(prefijo);
        }

        if (materiaDTO.getPlanEstudioId() != null && !materiaDTO.getPlanEstudioId().isBlank()) {
            PlanEstudio planEstudio = planEstudioRepository.findById(materiaDTO.getPlanEstudioId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Plan de estudio no encontrado con el Id: " + materiaDTO.getPlanEstudioId()));
            materia.setPlanEstudio(planEstudio);
        } else if (materiaDTO.getPlanEstudioId() != null && materiaDTO.getPlanEstudioId().isBlank()) {
            materia.setPlanEstudio(null);
        }

        Materia materiaActualizada = materiaRepository.save(materia);
        persistNestedHierarchy(materiaActualizada, materiaDTO.getUnidades());
        return toMateriaDetailDto(materiaActualizada);
    }

    @Override
    public void deleteMateria(String materiaId) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new RuntimeException("Materia no encontrado con el Id: " + materiaId));

        materiaRepository.delete(materia);
    }

    @Override
    public List<MateriaDTO> findAll() {
        return materiaRepository.findAll()
            .stream()
            .map(this::toMateriaDetailDto)
            .toList();
    }

    @Override
    public MateriaDTO getMateriaById(String materiaId) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new EntityNotFoundException("Materia no encontrado con el id: " + materiaId));

        return toMateriaDetailDto(materia);
    }
    
}
