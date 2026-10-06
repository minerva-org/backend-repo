package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.InstitucionDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Plantel;
import com.minerva.api.model.Institucion;
import com.minerva.api.repository.PlantelRepository;
import com.minerva.api.repository.InstitucionRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class InstitucionServiceImpl implements InstitucionService{

    private final InstitucionRepository institucionRepository;
    private final PlantelRepository plantelRepository;

    @Override
    public InstitucionDTO saveInstitucion(InstitucionDTO institucionDTO) {
        String nombre = institucionDTO.getNombre().trim();
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (institucionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una institucion con ese nombre");
        }

        Institucion institucion = new Institucion();
        institucion.setNombre(nombre);

        Institucion institucionGuardada = institucionRepository.save(institucion);
        return Mapper.toDTO(institucionGuardada);
    }

    @Override
    public InstitucionDTO updateInstitucion(Long institucionId, InstitucionDTO institucionDTO) {
        Institucion institucion = institucionRepository.findById(institucionId)
            .orElseThrow(() -> new EntityNotFoundException("Institucion no encontrado con el id: " + institucionId));

        if(institucionDTO.getNombre() != null){
            String nombre = institucionDTO.getNombre().trim();
            if(nombre.isEmpty()){
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            if(institucionRepository.existsByNombreIgnoreCaseAndIdNot(nombre, institucionId)){
                throw new IllegalArgumentException("Ya existe una institucion con ese nombre");
            }
            institucion.setNombre(nombre);
        }

        Institucion institucionActualizada = institucionRepository.save(institucion);
        return Mapper.toDTO(institucionActualizada);
    }

    //Bajas
    @Override
    public void deleteInstitucion(Long institucionId) {
        Institucion institucion = institucionRepository.findById(institucionId)
            .orElseThrow(() -> new EntityNotFoundException("La no se ha encontrado la Institucion con la id: " + institucionId ));

        institucionRepository.delete(institucion);
    }

    @Override
    @Transactional 
    public void softDeleteInstitucion(Long institucionId) {
        Institucion institucion = institucionRepository.findById(institucionId)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se ha encontrado la Institucion con el Id: " + institucionId));

        institucion.setActivo(false);
        institucionRepository.save(institucion);

        List<Plantel> planteles = plantelRepository.findByInstitucionIdAndActivoTrue(institucionId);
        planteles.forEach(p -> p.setActivo(false));
        plantelRepository.saveAll(planteles);
    }

    //Consultas
    @Override
    public List<InstitucionDTO> findAll() {
        return institucionRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    public InstitucionDTO getInstitucionById(Long institucionId) {
        Institucion institucion = institucionRepository.findById(institucionId)
            .orElseThrow(() -> new EntityNotFoundException());
            return Mapper.toDTO(institucion);
    }
    
}
