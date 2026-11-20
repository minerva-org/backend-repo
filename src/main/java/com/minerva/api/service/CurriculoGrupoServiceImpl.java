package com.minerva.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.ConceptoGrupoDTO;
import com.minerva.api.dto.ContenidoGrupoDTO;
import com.minerva.api.dto.CurriculoGrupoDTO;
import com.minerva.api.dto.FuenteGrupoDTO;
import com.minerva.api.dto.TemaGrupoDTO;
import com.minerva.api.dto.UnidadGrupoDTO;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.ConceptoGrupo;
import com.minerva.api.model.ContenidoGrupo;
import com.minerva.api.model.FuentesGrupo;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Tema;
import com.minerva.api.model.TemaGrupo;
import com.minerva.api.model.Unidad;
import com.minerva.api.model.UnidadGrupo;
import com.minerva.api.repository.ConceptoGrupoRepository;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.ContenidoGrupoRepository;
import com.minerva.api.repository.FuentesGrupoRepository;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.TemaGrupoRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadGrupoRepository;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.repository.UnidadRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurriculoGrupoServiceImpl implements CurriculoGrupoService {

    private final GrupoRepository grupoRepository;
    private final UnidadRepository unidadRepository;
    private final TemaRepository temaRepository;
    private final ConceptoRepository conceptoRepository;
    private final UnidadGrupoRepository unidadGrupoRepository;
    private final TemaGrupoRepository temaGrupoRepository;
    private final ConceptoGrupoRepository conceptoGrupoRepository;
    private final ContenidoGrupoRepository contenidoGrupoRepository;
    private final FuentesGrupoRepository fuentesGrupoRepository;

    @Override
    @Transactional(readOnly = true)
    public CurriculoGrupoDTO findByGrupoId(String grupoId) {
        buscarGrupo(grupoId);

        List<UnidadGrupoDTO> unidades = unidadGrupoRepository
                .findByGrupoIdAndActivoTrueOrderByIdAsc(grupoId)
                .stream()
                .map(this::toUnidadDetalle)
                .toList();

        List<FuenteGrupoDTO> fuentes = fuentesGrupoRepository
                .findByGrupoIdOrderByDocumentoCreatedAtDesc(grupoId)
                .stream()
                .map(this::toFuenteDto)
                .toList();

        return CurriculoGrupoDTO.builder()
                .idGrupo(grupoId)
                .unidades(unidades)
                .fuentes(fuentes)
                .build();
    }

    @Override
    @Transactional
    public CurriculoGrupoDTO replaceCurriculo(String grupoId, CurriculoGrupoDTO curriculo) {
        Grupo grupo = buscarGrupo(grupoId);
        if (curriculo == null) {
            throw new IllegalArgumentException("El curriculo es obligatorio");
        }

        for (UnidadGrupo unidadGrupo : unidadGrupoRepository.findByGrupoIdAndActivoTrueOrderByIdAsc(grupoId)) {
            unidadGrupo.setActivo(false);
            unidadGrupoRepository.save(unidadGrupo);
        }

        if (curriculo.getUnidades() == null) {
            curriculo.setUnidades(new ArrayList<>());
        }

        for (UnidadGrupoDTO unidadDto : curriculo.getUnidades()) {
            if (unidadDto == null || unidadDto.getNombre() == null || unidadDto.getNombre().isBlank()) {
                throw new IllegalArgumentException("Toda unidad debe tener nombre");
            }

            UnidadGrupo unidadGrupo = unidadDto.getId() == null || unidadDto.getId().isBlank()
                    ? new UnidadGrupo()
                    : unidadGrupoRepository.findById(unidadDto.getId().trim())
                            .filter(unidadExistente -> grupoId.equals(unidadExistente.getGrupo().getId()))
                            .orElse(new UnidadGrupo());

            unidadGrupo.setId(unidadGrupo.getId() == null || unidadGrupo.getId().isBlank()
                    ? UUID.randomUUID().toString()
                    : unidadGrupo.getId());
            unidadGrupo.setNombre(unidadDto.getNombre().trim());
            unidadGrupo.setGrupo(grupo);
            unidadGrupo.setMateria(grupo.getMateria());
            unidadGrupo.setUnidadOrigen(buscarUnidadOrigen(unidadDto.getIdUnidadOrigen()));
            unidadGrupo.setActivo(unidadDto.getActivo() == null || unidadDto.getActivo());
            unidadGrupo = unidadGrupoRepository.save(unidadGrupo);

            reemplazarTemas(unidadGrupo, unidadDto.getTemas());
        }

        return findByGrupoId(grupoId);
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadGrupoDTO findUnidadById(String unidadGrupoId) {
        UnidadGrupo unidadGrupo = unidadGrupoRepository.findById(unidadGrupoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Unidad de grupo no encontrada con el id: " + unidadGrupoId));
        return toUnidadDetalle(unidadGrupo);
    }

    private Grupo buscarGrupo(String grupoId) {
        return grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));
    }

    private Unidad buscarUnidadOrigen(String idUnidadOrigen) {
        if (idUnidadOrigen == null || idUnidadOrigen.isBlank()) {
            return null;
        }
        return unidadRepository.findById(idUnidadOrigen.trim()).orElse(null);
    }

    private Tema buscarTemaOrigen(String idTemaOrigen) {
        if (idTemaOrigen == null || idTemaOrigen.isBlank()) {
            return null;
        }
        return temaRepository.findById(idTemaOrigen.trim()).orElse(null);
    }

    private Concepto buscarConceptoOrigen(String idConceptoOrigen) {
        if (idConceptoOrigen == null || idConceptoOrigen.isBlank()) {
            return null;
        }
        return conceptoRepository.findById(idConceptoOrigen.trim()).orElse(null);
    }

    private void reemplazarTemas(UnidadGrupo unidadGrupo, List<TemaGrupoDTO> temasDtos) {
        for (TemaGrupo temaGrupo : temaGrupoRepository
                .findByUnidadGrupoIdAndActivoTrueOrderByIdAsc(unidadGrupo.getId())) {
            temaGrupo.setActivo(false);
            temaGrupoRepository.save(temaGrupo);
        }

        if (temasDtos == null) return;

        for (TemaGrupoDTO temaDto : temasDtos) {
            if (temaDto == null || temaDto.getNombre() == null || temaDto.getNombre().isBlank()) {
                throw new IllegalArgumentException("Todo tema debe tener nombre");
            }

            TemaGrupo temaGrupo = temaDto.getId() == null || temaDto.getId().isBlank()
                    ? new TemaGrupo()
                    : temaGrupoRepository.findById(temaDto.getId().trim())
                            .filter(temaExistente -> unidadGrupo.getId().equals(temaExistente.getUnidadGrupo().getId()))
                            .orElse(new TemaGrupo());

            temaGrupo.setId(temaGrupo.getId() == null || temaGrupo.getId().isBlank()
                    ? UUID.randomUUID().toString()
                    : temaGrupo.getId());
            temaGrupo.setClave(normalizarVacio(temaDto.getClave()));
            temaGrupo.setNombre(temaDto.getNombre().trim());
            temaGrupo.setUnidadGrupo(unidadGrupo);
            temaGrupo.setTemaOrigen(buscarTemaOrigen(temaDto.getIdTemaOrigen()));
            temaGrupo.setActivo(temaDto.getActivo() == null || temaDto.getActivo());
            temaGrupo = temaGrupoRepository.save(temaGrupo);

            reemplazarConceptos(temaGrupo, temaDto.getConceptos());
        }
    }

    private void reemplazarConceptos(TemaGrupo temaGrupo, List<ConceptoGrupoDTO> conceptosDtos) {
        for (ConceptoGrupo conceptoGrupo : conceptoGrupoRepository
                .findByTemaGrupoIdAndActivoTrueOrderByIdAsc(temaGrupo.getId())) {
            conceptoGrupo.setActivo(false);
            conceptoGrupoRepository.save(conceptoGrupo);
        }

        if (conceptosDtos == null) return;

        for (ConceptoGrupoDTO conceptoDto : conceptosDtos) {
            if (conceptoDto == null || conceptoDto.getNombre() == null || conceptoDto.getNombre().isBlank()) {
                throw new IllegalArgumentException("Todo concepto debe tener nombre");
            }

            ConceptoGrupo conceptoGrupo = conceptoDto.getId() == null || conceptoDto.getId().isBlank()
                    ? new ConceptoGrupo()
                    : conceptoGrupoRepository.findById(conceptoDto.getId().trim())
                            .filter(conceptoExistente ->
                                    temaGrupo.getId().equals(conceptoExistente.getTemaGrupo().getId()))
                            .orElse(new ConceptoGrupo());

            conceptoGrupo.setId(conceptoGrupo.getId() == null || conceptoGrupo.getId().isBlank()
                    ? UUID.randomUUID().toString()
                    : conceptoGrupo.getId());
            conceptoGrupo.setNombre(conceptoDto.getNombre().trim());
            conceptoGrupo.setTemaGrupo(temaGrupo);
            conceptoGrupo.setConceptoOrigen(buscarConceptoOrigen(conceptoDto.getIdConceptoOrigen()));
            conceptoGrupo.setActivo(conceptoDto.getActivo() == null || conceptoDto.getActivo());
            conceptoGrupo = conceptoGrupoRepository.save(conceptoGrupo);

            reemplazarContenidos(conceptoGrupo, conceptoDto.getContenidos());
        }
    }

    private void reemplazarContenidos(ConceptoGrupo conceptoGrupo, List<ContenidoGrupoDTO> contenidosDtos) {
        for (ContenidoGrupo contenido : contenidoGrupoRepository
                .findByConceptoGrupoIdOrderByIdAsc(conceptoGrupo.getId())) {
            contenidoGrupoRepository.delete(contenido);
        }

        if (contenidosDtos == null) return;

        for (ContenidoGrupoDTO contenidoDto : contenidosDtos) {
            if (contenidoDto == null || contenidoDto.getDescripcion() == null
                    || contenidoDto.getDescripcion().isBlank()) {
                throw new IllegalArgumentException("Todo contenido debe tener descripcion");
            }

            ContenidoGrupo contenido = new ContenidoGrupo();
            contenido.setId(contenidoDto.getId() == null || contenidoDto.getId().isBlank()
                    ? UUID.randomUUID().toString()
                    : contenidoDto.getId().trim());
            contenido.setDescripcion(contenidoDto.getDescripcion().trim());
            contenido.setConceptoGrupo(conceptoGrupo);
            contenidoGrupoRepository.save(contenido);
        }
    }

    private UnidadGrupoDTO toUnidadDetalle(UnidadGrupo unidadGrupo) {
        List<TemaGrupoDTO> temas = temaGrupoRepository
                .findByUnidadGrupoIdAndActivoTrueOrderByIdAsc(unidadGrupo.getId())
                .stream()
                .map(this::toTemaDetalle)
                .toList();

        return UnidadGrupoDTO.builder()
                .id(unidadGrupo.getId())
                .nombre(unidadGrupo.getNombre())
                .idMateria(unidadGrupo.getMateria() != null ? unidadGrupo.getMateria().getId() : null)
                .idGrupo(unidadGrupo.getGrupo().getId())
                .idUnidadOrigen(unidadGrupo.getUnidadOrigen() != null
                        ? unidadGrupo.getUnidadOrigen().getId()
                        : null)
                .activo(unidadGrupo.getActivo())
                .temas(temas)
                .build();
    }

    private TemaGrupoDTO toTemaDetalle(TemaGrupo temaGrupo) {
        List<ConceptoGrupoDTO> conceptos = conceptoGrupoRepository
                .findByTemaGrupoIdAndActivoTrueOrderByIdAsc(temaGrupo.getId())
                .stream()
                .map(this::toConceptoDetalle)
                .toList();

        return TemaGrupoDTO.builder()
                .id(temaGrupo.getId())
                .clave(temaGrupo.getClave())
                .nombre(temaGrupo.getNombre())
                .idUnidadGrupo(temaGrupo.getUnidadGrupo().getId())
                .idTemaOrigen(temaGrupo.getTemaOrigen() != null
                        ? temaGrupo.getTemaOrigen().getId()
                        : null)
                .activo(temaGrupo.getActivo())
                .conceptos(conceptos)
                .build();
    }

    private ConceptoGrupoDTO toConceptoDetalle(ConceptoGrupo conceptoGrupo) {
        List<ContenidoGrupoDTO> contenidos = contenidoGrupoRepository
                .findByConceptoGrupoIdOrderByIdAsc(conceptoGrupo.getId())
                .stream()
                .map(contenido -> ContenidoGrupoDTO.builder()
                        .id(contenido.getId())
                        .descripcion(contenido.getDescripcion())
                        .idConceptoGrupo(conceptoGrupo.getId())
                        .build())
                .toList();

        return ConceptoGrupoDTO.builder()
                .id(conceptoGrupo.getId())
                .nombre(conceptoGrupo.getNombre())
                .idTemaGrupo(conceptoGrupo.getTemaGrupo().getId())
                .idConceptoOrigen(conceptoGrupo.getConceptoOrigen() != null
                        ? conceptoGrupo.getConceptoOrigen().getId()
                        : null)
                .activo(conceptoGrupo.getActivo())
                .contenidos(contenidos)
                .build();
    }

    private FuenteGrupoDTO toFuenteDto(FuentesGrupo fuente) {
        return FuenteGrupoDTO.builder()
                .idDocumento(fuente.getId().getIdDocumento())
                .idGrupo(fuente.getId().getIdGrupo())
                .documento(Mapper.toDTO(fuente.getDocumento()))
                .build();
    }

    private String normalizarVacio(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
