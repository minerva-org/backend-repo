package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.UniversidadDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Plantel;
import com.minerva.api.model.Universidad;
import com.minerva.api.repository.PlantelRepository;
import com.minerva.api.repository.UniversidadRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UniversidadServiceImpl implements UniversidadService{

    private final UniversidadRepository universidadRepository;
    private final PlantelRepository plantelRepository;

    @Override
    public UniversidadDTO saveUniversidad(UniversidadDTO universidadDTO) {
        String nombre = universidadDTO.getNombre().trim();
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (universidadRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una universidad con ese nombre");
        }

        Universidad universidad = new Universidad();
        universidad.setNombre(nombre);

        Universidad universidadGuardada = universidadRepository.save(universidad);
        return Mapper.toDTO(universidadGuardada);
    }

    @Override
    public UniversidadDTO updateUniversidad(Long universidadId, UniversidadDTO universidadDTO) {
        Universidad universidad = universidadRepository.findById(universidadId)
            .orElseThrow(() -> new EntityNotFoundException("Universidad no encontrado con el id: " + universidadId));

        if(universidadDTO.getNombre() != null){
            String nombre = universidadDTO.getNombre().trim();
            if(nombre.isEmpty()){
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            if(universidadRepository.existsByNombreIgnoreCaseAndIdNot(nombre, universidadId)){
                throw new IllegalArgumentException("Ya existe una universidad con ese nombre");
            }
            universidad.setNombre(nombre);
        }

        Universidad universidadActualizada = universidadRepository.save(universidad);
        return Mapper.toDTO(universidadActualizada);
    }

    //Bajas
    @Override
    public void deleteUniversidad(Long universidadId) {
        Universidad universidad = universidadRepository.findById(universidadId)
            .orElseThrow(() -> new EntityNotFoundException("La no se ha encontrado la Universidad con la id: " + universidadId ));

        universidadRepository.delete(universidad);
    }

    @Override
    @Transactional 
    public void softDeleteUniversidad(Long universidadId) {
        Universidad universidad = universidadRepository.findById(universidadId)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se ha encontrado la Universidad con el Id: " + universidadId));

        universidad.setActivo(false);
        universidadRepository.save(universidad);

        List<Plantel> planteles = plantelRepository.findByUniversidadIdAndActivoTrue(universidadId);
        planteles.forEach(p -> p.setActivo(false));
        plantelRepository.saveAll(planteles);
    }

    //Consultas
    @Override
    public List<UniversidadDTO> findAll() {
        return universidadRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    public UniversidadDTO getUniversidadById(Long universidadId) {
        Universidad universidad = universidadRepository.findById(universidadId)
            .orElseThrow(() -> new EntityNotFoundException());
            return Mapper.toDTO(universidad);
    }
    
}
