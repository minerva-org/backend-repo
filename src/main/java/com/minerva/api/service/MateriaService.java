package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.MateriaDTO;
public interface MateriaService {
    
    MateriaDTO saveMateria(MateriaDTO materiaDTO);

    MateriaDTO updateMateria(String materiaId, MateriaDTO materiaDTO);

    MateriaDTO updateMateria(String materiaId, MateriaDTO materiaDTO, Long plantelId);
    
    void updateEstado(String materialId, Boolean estado);

    void deleteMateria(String materiaId);
    
    List<MateriaDTO> findAll();

    List<MateriaDTO>findAllByIdplantel(Long plantelId);

    MateriaDTO getMateriaById(String materiaId);
}
