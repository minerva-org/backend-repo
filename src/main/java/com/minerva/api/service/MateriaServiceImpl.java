package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Materia;
import com.minerva.api.model.PlanEstudio;
import com.minerva.api.repository.MateriaRepository;
import com.minerva.api.repository.PlanEstudioRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MateriaServiceImpl implements MateriaService{

    private final MateriaRepository materiaRepository;
    private final PlanEstudioRepository planEstudioRepository;

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
        if (planEstudioId == null) {
            throw new IllegalArgumentException("El plan de estudio es obligatorio");
        }
        PlanEstudio planEstudio = planEstudioRepository.findById(planEstudioId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Plan de estudio no encontrado con el Id: " + planEstudioId));

        Materia materia = new Materia();
        materia.setId(materiaDTO.getId());
        materia.setNombre(nombre);
        materia.setPrefijo(materiaDTO.getPrefijo());
        materia.setPlanEstudio(planEstudio);

        Materia materiaGuardada = materiaRepository.save(materia);
        return Mapper.toDTO(materiaGuardada);
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

        if (materiaDTO.getPlanEstudioId() != null) {
            PlanEstudio planEstudio = planEstudioRepository.findById(materiaDTO.getPlanEstudioId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Plan de estudio no encontrado con el Id: " + materiaDTO.getPlanEstudioId()));
            materia.setPlanEstudio(planEstudio);
        }

        Materia materiaActualizada = materiaRepository.save(materia);
        return Mapper.toDTO(materiaActualizada);
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
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    public MateriaDTO getMateriaById(String materiaId) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new EntityNotFoundException("Materia no encontrado con el id: " + materiaId));

        return Mapper.toDTO(materia);
    }
    
}
