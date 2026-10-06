package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.ConceptoDTO;
import com.minerva.api.dto.TemaDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Tema;
import com.minerva.api.model.Unidad;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemaServiceImpl implements TemaService {

    private final UnidadRepository unidadRepository;
    private final TemaRepository temaRepository;
    private final ConceptoRepository conceptoRepository;

    private TemaDTO toTemaDetailDto(Tema tema) {
        TemaDTO dto = Mapper.toDTO(tema);
        if (dto == null) return null;
        dto.setConceptos(conceptoRepository.findByTemaId(tema.getId()).stream()
            .map(Mapper::toDTO)
            .toList());
        return dto;
    }

    @Override
    @Transactional
    public TemaDTO saveTema(TemaDTO temaDTO) {
        if (temaDTO.getId() == null || temaDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = temaDTO.getId().trim();
        if (temaRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un tema con ese Id: " + id);
        }

        if (temaDTO.getNombre() == null || temaDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String nombre = temaDTO.getNombre().trim();

        if (temaDTO.getUnidadId() == null || temaDTO.getUnidadId().isBlank()) {
            throw new IllegalArgumentException("La unidad es obligatoria");
        }
        String unidadId = temaDTO.getUnidadId().trim();
        Unidad unidad = unidadRepository.findById(unidadId)
            .orElseThrow(() -> new EntityNotFoundException("Unidad no encontrada con el id: " + unidadId));

        if (temaRepository.existsByNombreIgnoreCaseAndUnidadId(nombre, unidadId)) {
            throw new IllegalArgumentException("Ya existe un tema con ese nombre en esta unidad");
        }

        Tema tema = new Tema();
        tema.setId(id);
        tema.setNombre(nombre);
        tema.setUnidad(unidad);

        return toTemaDetailDto(temaRepository.save(tema));
    }

    @Override
    @Transactional
    public TemaDTO updateTema(String temaId, TemaDTO temaDTO) {
        Tema tema = temaRepository.findById(temaId)
            .orElseThrow(() -> new EntityNotFoundException("Tema no encontrado con el id: " + temaId));

        Unidad unidadDestino = tema.getUnidad();
        if (temaDTO.getUnidadId() != null) {
            String unidadId = temaDTO.getUnidadId().trim();
            unidadDestino = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new EntityNotFoundException("No existe una unidad con el id: " + unidadId));
        }

        String nombreDestino = tema.getNombre();
        if (temaDTO.getNombre() != null) {
            nombreDestino = temaDTO.getNombre().trim();
            if (nombreDestino.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
        }

        if (temaRepository.existsByNombreIgnoreCaseAndUnidadIdAndIdNot(
                nombreDestino, unidadDestino.getId(), temaId)) {
            throw new IllegalArgumentException("Ya existe un tema con ese nombre en esta unidad");
        }

        tema.setNombre(nombreDestino);
        tema.setUnidad(unidadDestino);

        return toTemaDetailDto(temaRepository.save(tema));
    }

    @Override
    @Transactional
    public void deleteTema(String temaId) {
        Tema tema = temaRepository.findById(temaId)
            .orElseThrow(() -> new EntityNotFoundException("Tema no encontrado con el id: " + temaId));

        temaRepository.delete(tema);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemaDTO> findAll() {
        return temaRepository.findAll()
            .stream()
            .map(this::toTemaDetailDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TemaDTO getTemaById(String temaId) {
        Tema tema = temaRepository.findById(temaId)
            .orElseThrow(() -> new EntityNotFoundException("Tema no encontrado con el id: " + temaId));

        return toTemaDetailDto(tema);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemaDTO> findAllByUnidadId(String unidadId) {
        List<Tema> temas;
        if (unidadId == null) {
            temas = temaRepository.findAll();
        } else {
            if (!unidadRepository.existsById(unidadId)) {
                throw new EntityNotFoundException("Unidad no encontrada con el id: " + unidadId);
            }
            temas = temaRepository.findByUnidadId(unidadId);
        }
        return temas.stream().map(this::toTemaDetailDto).toList();
    }
}