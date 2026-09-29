package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.UnidadDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Unidad;
import com.minerva.api.repository.MateriaRepository;
import com.minerva.api.repository.UnidadRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UnidadServiceImpl implements UnidadService{

    private final UnidadRepository unidadRepository;
    private final MateriaRepository materiaRepository;

    @Override
    @Transactional
    public UnidadDTO saveUnidad(UnidadDTO unidadDTO) {
        if (unidadDTO.getId() == null || unidadDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = unidadDTO.getId().trim();
        if (unidadRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe una unidad con ese Id: " + id);
        }

        if (unidadDTO.getNombre() == null || unidadDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String nombre = unidadDTO.getNombre().trim();

        if (unidadDTO.getIdMateria() == null || unidadDTO.getIdMateria().isBlank()) {
            throw new IllegalArgumentException("La materia es obligatoria");
        }
        String materiaId = unidadDTO.getIdMateria().trim();
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new EntityNotFoundException("Materia no encontrada con el id: " + materiaId));

        if (unidadRepository.existsByNombreIgnoreCaseAndMateriaId(nombre, materiaId)) {
            throw new IllegalArgumentException("Ya existe una unidad con ese nombre en esta materia");
        }

        Unidad unidad = new Unidad();
        unidad.setId(id);
        unidad.setNombre(nombre);
        unidad.setMateria(materia);

        return Mapper.toDTO(unidadRepository.save(unidad));
    }

    @Override
    @Transactional
    public UnidadDTO updateUnidad(String unidadId, UnidadDTO unidadDTO) {
        Unidad unidad = unidadRepository.findById(unidadId)
            .orElseThrow(() -> new EntityNotFoundException("Unidad no encontrada con el id: " + unidadId));

        Materia materiaDestino = unidad.getMateria();
        if (unidadDTO.getIdMateria() != null) {
            String materiaId = unidadDTO.getIdMateria().trim();
            materiaDestino = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new EntityNotFoundException("No existe una materia con el id: " + materiaId));
        }

        String nombreDestino = unidad.getNombre();
        if (unidadDTO.getNombre() != null) {
            nombreDestino = unidadDTO.getNombre().trim();
            if (nombreDestino.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
        }

        if (unidadRepository.existsByNombreIgnoreCaseAndMateriaIdAndIdNot(
                nombreDestino, materiaDestino.getId(), unidadId)) {
            throw new IllegalArgumentException("Ya existe una unidad con ese nombre en esta materia");
        }

        unidad.setNombre(nombreDestino);
        unidad.setMateria(materiaDestino);

        return Mapper.toDTO(unidadRepository.save(unidad));
    }

    @Override
    public void deleteUnidad(String unidadId) {
        Unidad unidad = unidadRepository.findById(unidadId)
            .orElseThrow(() -> new EntityNotFoundException("Unidad no encontrada con el id: " + unidadId));
        
        unidadRepository.delete(unidad);
    }

    @Override
    public List<UnidadDTO> findAll() {
        return unidadRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    public UnidadDTO getUnidadById(String unidadId) {
        Unidad unidad = unidadRepository.findById(unidadId)
            .orElseThrow(() -> new EntityNotFoundException("Unidad no encontrada con el id: " + unidadId));

        return Mapper.toDTO(unidad);
    }

    @Override
    @Transactional
    public List<UnidadDTO> findAllByMateriaId(String materiaId) {
        List<Unidad> unidades;
        if (materiaId == null) {
            unidades = unidadRepository.findAll();
        } else {
            if (!materiaRepository.existsById(materiaId)) {
                throw new EntityNotFoundException("Materia no encontrada con el id: " + materiaId);
            }
            unidades = unidadRepository.findByMateriaId(materiaId);
        }
        return unidades.stream().map(Mapper::toDTO).toList();
    }
    
}
