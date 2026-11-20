package com.minerva.api.service;
import java.util.List;
import com.minerva.api.User.User;
import com.minerva.api.dto.MateriaDTO;
public interface MateriaService {
    
    MateriaDTO saveMateria(MateriaDTO materiaDTO, User usuario);

    MateriaDTO updateMateria(String materiaId, MateriaDTO materiaDTO, Long plantelId, User usuario);
    
    void updateEstado(String materialId, Boolean estado, User usuario);

    void deleteMateria(String materiaId, User usuario);
    
    List<MateriaDTO> findAll();

    List<MateriaDTO> findAllByIdplantel(Long plantelId);

    List<MateriaDTO> findAllByIdplantel(Long plantelId, User usuario);

    List<MateriaDTO> findMateriasVisibles(Long plantelId, User usuario);

    MateriaDTO getMateriaById(String materiaId);
}
