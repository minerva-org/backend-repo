package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.ConceptoDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Tema;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.TemaRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ConceptoServiceImpl implements ConceptoService {
    private final TemaRepository temaRepository;
    private final ConceptoRepository conceptoRepository;

    @Override
    @Transactional
    public ConceptoDTO saveConcepto(ConceptoDTO ConceptoDTO) {
        if (ConceptoDTO.getId() == null || ConceptoDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = ConceptoDTO.getId().trim();
        if (conceptoRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un Concepto con ese Id: " + id);
        }

        if (ConceptoDTO.getNombre() == null || ConceptoDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String nombre = ConceptoDTO.getNombre().trim();

        if (ConceptoDTO.getTemaId() == null || ConceptoDTO.getTemaId().isBlank()) {
            throw new IllegalArgumentException("El Tema es obligatoria");
        }
        String temaId = ConceptoDTO.getTemaId().trim();
        Tema tema = temaRepository.findById(temaId)
            .orElseThrow(() -> new EntityNotFoundException("Tema no encontrada con el id: " + temaId));

        if (conceptoRepository.existsByNombreIgnoreCaseAndTemaId(nombre, temaId)) {
            throw new IllegalArgumentException("Ya existe un Concepto con ese nombre en esta Tema");
        }

        Concepto concepto = new Concepto();
        concepto.setId(id);
        concepto.setNombre(nombre);
        concepto.setTema(tema);

        return Mapper.toDTO(conceptoRepository.save(concepto));
    }

    @Override
    @Transactional
    public ConceptoDTO updateConcepto(String conceptoId, ConceptoDTO conceptoDTO) {
        Concepto concepto = conceptoRepository.findById(conceptoId)
            .orElseThrow(() -> new EntityNotFoundException("Concepto no encontrado con el id: " + conceptoId));

        Tema temaDestino = concepto.getTema();
        if (conceptoDTO.getTemaId() != null) {
            String temaId = conceptoDTO.getTemaId().trim();
            temaDestino = temaRepository.findById(temaId)
                .orElseThrow(() -> new EntityNotFoundException("No existe una Tema con el id: " + temaId));
        }

        String nombreDestino = concepto.getNombre();
        if (conceptoDTO.getNombre() != null) {
            nombreDestino = conceptoDTO.getNombre().trim();
            if (nombreDestino.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
        }

        if (conceptoRepository.existsByNombreIgnoreCaseAndTemaIdAndIdNot(
                nombreDestino, temaDestino.getId(), conceptoId)) {
            throw new IllegalArgumentException("Ya existe un Concepto con ese nombre en esta Tema");
        }

        concepto.setNombre(nombreDestino);
        concepto.setTema(temaDestino);

        return Mapper.toDTO(conceptoRepository.save(concepto));
    }

    @Override
    @Transactional
    public void deleteConcepto(String conceptoId) {
        Concepto concepto = conceptoRepository.findById(conceptoId)
            .orElseThrow(() -> new EntityNotFoundException("Concepto no encontrado con el id: " + conceptoId));

        conceptoRepository.delete(concepto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConceptoDTO> findAll() {
        return conceptoRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConceptoDTO getConceptoById(String conceptoId) {
        Concepto concepto = conceptoRepository.findById(conceptoId)
            .orElseThrow(() -> new EntityNotFoundException("Concepto no encontrado con el id: " + conceptoId));

        return Mapper.toDTO(concepto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConceptoDTO> findAllByTemaId(String temaId) {
        List<Concepto> Conceptos;
        if (temaId == null) {
            Conceptos = conceptoRepository.findAll();
        } else {
            if (!temaRepository.existsById(temaId)) {
                throw new EntityNotFoundException("Tema no encontrada con el id: " + temaId);
            }
            Conceptos = conceptoRepository.findByTemaId(temaId);
        }
        return Conceptos.stream().map(Mapper::toDTO).toList();
    }
    
}
