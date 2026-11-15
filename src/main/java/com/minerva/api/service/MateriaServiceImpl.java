package com.minerva.api.service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.minerva.api.repository.PlantelRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MateriaServiceImpl implements MateriaService{

    private final MateriaRepository materiaRepository;
    private final PlanEstudioRepository planEstudioRepository;
    private final PlantelRepository plantelRepository;
    private final UnidadRepository unidadRepository;
    private final TemaRepository temaRepository;
    private final ConceptoRepository conceptoRepository;

    private String nextId() {
        return UUID.randomUUID().toString();
    }

    private String toPlantelValue(Long plantelId) {
        return plantelId == null ? null : plantelId.toString();
    }

    private boolean existsByNombreInScope(String nombre, String plantelId) {
        if (plantelId == null) {
            return materiaRepository.existsByNombreIgnoreCaseAndPlantelIdIsNull(nombre);
        }
        return materiaRepository.existsByNombreIgnoreCaseAndPlantelId(nombre, plantelId);
    }

    private boolean existsByNombreInScopeAndIdNot(String nombre, Long plantelId, String materiaId) {
        if (plantelId == null) {
            return materiaRepository.existsByNombreIgnoreCaseAndPlantelIdIsNullAndIdNot(nombre, materiaId);
        }
        return materiaRepository.existsByNombreIgnoreCaseAndPlantelIdAndIdNot(nombre, plantelId, materiaId);
    }

    private void copyNestedHierarchy(Materia sourceMateria, Materia targetMateria) {
        List<Unidad> unidadesBase = unidadRepository.findByMateriaId(sourceMateria.getId());
        for (Unidad unidadBase : unidadesBase) {
            Unidad unidadCopia = new Unidad();
            unidadCopia.setId(nextId());
            unidadCopia.setNombre(unidadBase.getNombre());
            unidadCopia.setActivo(unidadBase.getActivo());
            unidadCopia.setMateria(targetMateria);
            Unidad unidadGuardada = unidadRepository.save(unidadCopia);

            List<Tema> temasBase = temaRepository.findByUnidadId(unidadBase.getId());
            for (Tema temaBase : temasBase) {
                Tema temaCopia = new Tema();
                temaCopia.setId(nextId());
                temaCopia.setNombre(temaBase.getNombre());
                temaCopia.setActivo(temaBase.getActivo());
                temaCopia.setUnidad(unidadGuardada);
                Tema temaGuardado = temaRepository.save(temaCopia);

                List<Concepto> conceptosBase = conceptoRepository.findByTemaId(temaBase.getId());
                for (Concepto conceptoBase : conceptosBase) {
                    Concepto conceptoCopia = new Concepto();
                    conceptoCopia.setId(nextId());
                    conceptoCopia.setNombre(conceptoBase.getNombre());
                    conceptoCopia.setActivo(conceptoBase.getActivo());
                    conceptoCopia.setTema(temaGuardado);
                    conceptoRepository.save(conceptoCopia);
                }
            }
        }
    }

    private Materia createPlantelOverrideIfNeeded(Materia materiaBase, Long plantelId) {
        return materiaRepository.findByPlantelIdAndMateriaBaseId(plantelId, materiaBase.getId())
            .orElseGet(() -> {
                Materia copia = new Materia();
                copia.setId(nextId());
                copia.setNombre(materiaBase.getNombre());
                copia.setPrefijo(materiaBase.getPrefijo());
                copia.setPlanEstudio(materiaBase.getPlanEstudio());
                copia.setPlantelId(plantelId);
                copia.setMateriaBaseId(materiaBase.getId());
                copia.setActivo(materiaBase.getActivo());

                Materia copiaGuardada = materiaRepository.save(copia);
                copyNestedHierarchy(materiaBase, copiaGuardada);
                return copiaGuardada;
            });
    }

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
        String plantelId = toPlantelValue(materiaDTO.getPlantelId());
        if (existsByNombreInScope(nombre, plantelId)) {
            throw new IllegalArgumentException("Ya existe una materia con ese nombre");
        }
        if (materiaDTO.getPlantelId() != null && !plantelRepository.existsById(materiaDTO.getPlantelId())) {
            throw new EntityNotFoundException("Plantel no encontrado con el id: " + materiaDTO.getPlantelId());
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
        materia.setPlantelId(materiaDTO.getPlantelId());
        materia.setMateriaBaseId(materiaDTO.getMateriaBaseId());
        materia.setActivo(materiaDTO.getActivo() != null ? materiaDTO.getActivo() : true);

        Materia materiaGuardada = materiaRepository.save(materia);
        persistNestedHierarchy(materiaGuardada, materiaDTO.getUnidades());
        return toMateriaDetailDto(materiaGuardada);
    }

    @Override
    @Transactional
    public MateriaDTO updateMateria(String materiaId, MateriaDTO materiaDTO) {
        return updateMateria(materiaId, materiaDTO, null);
    }

    @Override
    @Transactional
    public MateriaDTO updateMateria(String materiaId, MateriaDTO materiaDTO, Long plantelId) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Materia no encontrada con el Id: " + materiaId));

        Materia materiaObjetivo = materia;
        if (plantelId != null) {
            if (!plantelRepository.existsById(plantelId)) {
                throw new EntityNotFoundException("Plantel no encontrado con el id: " + plantelId);
            }
            String plantelValue = toPlantelValue(plantelId);
            if (materia.getPlantelId() == null) {
                materiaObjetivo = createPlantelOverrideIfNeeded(materia, plantelId);
            } else if (!materia.getPlantelId().equals(plantelValue)) {
                throw new IllegalArgumentException("La materia no pertenece al plantel indicado");
            }
        }

        if (materiaDTO.getNombre() != null) {
            String nombre = materiaDTO.getNombre().trim();
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            if (existsByNombreInScopeAndIdNot(nombre, materiaObjetivo.getPlantelId(), materiaObjetivo.getId())) {
                throw new IllegalArgumentException("Ya existe una materia con ese nombre");
            }
            materiaObjetivo.setNombre(nombre);
        }

        if (materiaDTO.getPrefijo() != null) {
            String prefijo = materiaDTO.getPrefijo().trim();
            if (prefijo.isEmpty()) {
                throw new IllegalArgumentException("El prefijo no puede estar vacío");
            }
            materiaObjetivo.setPrefijo(prefijo);
        }

        if (materiaDTO.getPlanEstudioId() != null && !materiaDTO.getPlanEstudioId().isBlank()) {
            PlanEstudio planEstudio = planEstudioRepository.findById(materiaDTO.getPlanEstudioId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Plan de estudio no encontrado con el Id: " + materiaDTO.getPlanEstudioId()));
            materiaObjetivo.setPlanEstudio(planEstudio);
        } else if (materiaDTO.getPlanEstudioId() != null && materiaDTO.getPlanEstudioId().isBlank()) {
            materiaObjetivo.setPlanEstudio(null);
        }

        if(materiaDTO.getActivo() != null){
            materiaObjetivo.setActivo(materiaDTO.getActivo());
        }

        Materia materiaActualizada = materiaRepository.save(materiaObjetivo);
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
    @Transactional 
    public void updateEstado(String materialId, Boolean estado) {
        Materia materia = materiaRepository.findById(materialId)
            .orElseThrow(() -> new RuntimeException("Materia no encontrado con el Id: " + materialId));

                materia.setActivo(estado);
                unidadRepository.cambiarEstadoPorMateria(materialId, estado);
                temaRepository.cambiarEstadoPorMateria(materialId, estado);
                conceptoRepository.cambiarEstadoPorMateria(materialId, estado);
                materiaRepository.save(materia);
    }

    @Override
    public List<MateriaDTO> findAll() {
        return materiaRepository.findAll()
            .stream()
            .map(this::toMateriaDetailDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriaDTO> findAllByIdplantel(Long plantelId) {
        List<Materia> materiasBase = materiaRepository.findByPlantelIdIsNull();
        List<Materia> materiasDePlantel = materiaRepository.findByPlantelId(plantelId);

        // Mapeamos las materias base por su ID
        Map<String, Materia> resultadoPorId = new LinkedHashMap<>();
        for (Materia base : materiasBase) {
            if (base.getId() != null) {
                resultadoPorId.put(base.getId(), base);
            }
        }

        // reemplazamos y agregamos materias
        for (Materia materiaPlantel : materiasDePlantel) {
            String materiaBaseId = materiaPlantel.getMateriaBaseId();
            if (materiaBaseId != null) {
                resultadoPorId.remove(materiaBaseId);
            }
            if (materiaPlantel.getId() != null) {
                resultadoPorId.put(materiaPlantel.getId(), materiaPlantel);
            }
        }

        return resultadoPorId.values().stream()
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
