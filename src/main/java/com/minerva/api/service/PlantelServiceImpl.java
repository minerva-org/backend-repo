package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.PlantelDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Plantel;
import com.minerva.api.model.Institucion;
import com.minerva.api.repository.PlantelRepository;
import com.minerva.api.repository.InstitucionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Service 
public class PlantelServiceImpl implements PlantelService {
    private final InstitucionRepository institucionRepository; 
    private final PlantelRepository plantelRepository;

    //Inserciones
    @Override
    public PlantelDTO savePlantel(PlantelDTO planteldto) {
        Long Uid = planteldto.getInstitucionId();
        Institucion u = institucionRepository.findById(Uid)
            .orElseThrow(() -> new EntityNotFoundException("Institucion no encontrado con el Id: " + Uid));       

        if (plantelRepository.existsByNombreIgnoreCase(planteldto.getNombre())) {
        throw new IllegalArgumentException("Ya existe un plantel con ese nombre");
    }
            Plantel plan = new Plantel();
            plan.setNombre(planteldto.getNombre());
            plan.setDireccion(planteldto.getDireccion());
            plan.setInstitucion(u);
            plan.setActivo(planteldto.getActivo() != null ? planteldto.getActivo() : true);

            Plantel plantelGuardado = plantelRepository.save(plan);
            return Mapper.toDTO(plantelGuardado);
    }

    @Override
    public PlantelDTO updatePlantel(Long plantelId, PlantelDTO planteldto) {
        Plantel plan = plantelRepository.findById(plantelId)
            .orElseThrow(() -> new EntityNotFoundException("Plantel no encontrado con el id: " + plantelId));

        if (planteldto.getNombre() != null) {
            String nombre = planteldto.getNombre().trim();
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            if (plantelRepository.existsByNombreIgnoreCaseAndIdNot(nombre, plantelId)) {
                throw new IllegalArgumentException("Ya existe un plantel con ese nombre");
            }
            plan.setNombre(nombre);
        }

        if (planteldto.getInstitucionId() != null) {
            Institucion institucion = institucionRepository.findById(planteldto.getInstitucionId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Institucion no encontrado con el Id: " + planteldto.getInstitucionId()));
            plan.setInstitucion(institucion);
        }

        if (planteldto.getDireccion() != null) {
            plan.setDireccion(planteldto.getDireccion());
        }
        if (planteldto.getActivo() != null) {
            plan.setActivo(planteldto.getActivo());
        }

        Plantel plantelActualizado = plantelRepository.save(plan);
        return Mapper.toDTO(plantelActualizado);
    }

    //Borrados
    @Override
    public void deletePlantel(Long plantelId) {
        Plantel plan = plantelRepository.findById(plantelId)
            .orElseThrow(() -> new RuntimeException("Plantel no encontrado con el Id: " + plantelId));

        plantelRepository.delete(plan);
    }

    @Override
    public void softDeletePlantel(Long plantelId) {
        Plantel plan = plantelRepository.findById(plantelId)
            .orElseThrow(() -> new RuntimeException("Plantel no encontrado con el Id: " + plantelId));

        plan.setActivo(false);
        plantelRepository.save(plan);
    }

    //Consultas
    @Override
    public List<PlantelDTO> findAll() {
        return plantelRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override 
    public PlantelDTO getPlantelById(Long plantelId){
        Plantel plan = plantelRepository.findById(plantelId)
            .orElseThrow(() -> new EntityNotFoundException("Plantel no encontrado con el id: " + plantelId));

        return Mapper.toDTO(plan);
    }
}