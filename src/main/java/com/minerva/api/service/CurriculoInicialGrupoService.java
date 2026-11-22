package com.minerva.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.model.Concepto;
import com.minerva.api.model.ConceptoGrupo;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Tema;
import com.minerva.api.model.TemaGrupo;
import com.minerva.api.model.Unidad;
import com.minerva.api.model.UnidadGrupo;
import com.minerva.api.repository.ConceptoGrupoRepository;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.TemaGrupoRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadGrupoRepository;
import com.minerva.api.repository.UnidadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurriculoInicialGrupoService {

    private final UnidadRepository unidadRepository;
    private final TemaRepository temaRepository;
    private final ConceptoRepository conceptoRepository;
    private final UnidadGrupoRepository unidadGrupoRepository;
    private final TemaGrupoRepository temaGrupoRepository;
    private final ConceptoGrupoRepository conceptoGrupoRepository;

    @Transactional
    public void initializeFromMateria(Grupo grupo, Materia materia) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo es obligatorio.");
        }
        if (materia == null) {
            throw new IllegalArgumentException("La materia es obligatoria.");
        }
        if (materia.getId() == null || materia.getId().isBlank()) {
            throw new IllegalArgumentException("La materia debe tener un id valido.");
        }

        List<Unidad> unidades = unidadRepository.findByMateriaId(materia.getId())
                .stream()
                .filter(unidad -> Boolean.TRUE.equals(unidad.getActivo()))
                .toList();
        if (unidades.isEmpty()) {
            throw new IllegalArgumentException(
                    "La materia no tiene curriculo inicial con unidades.");
        }

        for (Unidad unidad : unidades) {
            if (unidad.getNombre() == null || unidad.getNombre().isBlank()) {
                throw new IllegalArgumentException(
                        "Toda unidad activa debe tener un nombre.");
            }
            List<Tema> temas = temaRepository.findByUnidadId(unidad.getId())
                    .stream()
                    .filter(tema -> Boolean.TRUE.equals(tema.getActivo()))
                    .toList();
            if (temas.isEmpty()) {
                throw new IllegalArgumentException(
                        "La unidad \"" + unidad.getNombre()
                                + "\" no tiene temas activos.");
            }

            for (Tema tema : temas) {
                if (tema.getNombre() == null || tema.getNombre().isBlank()) {
                    throw new IllegalArgumentException(
                            "Todo tema activo debe tener un nombre.");
                }
                List<Concepto> conceptos = conceptoRepository.findByTemaId(tema.getId())
                        .stream()
                        .filter(concepto -> Boolean.TRUE.equals(concepto.getActivo()))
                        .toList();
                if (conceptos.isEmpty()) {
                    throw new IllegalArgumentException(
                            "El tema \"" + tema.getNombre()
                                    + "\" no tiene conceptos activos.");
                }
            }
        }

        for (Unidad unidad : unidades) {
            UnidadGrupo unidadGrupo = new UnidadGrupo();
            unidadGrupo.setId(UUID.randomUUID().toString());
            unidadGrupo.setNombre(unidad.getNombre().trim());
            unidadGrupo.setMateria(materia);
            unidadGrupo.setGrupo(grupo);
            unidadGrupo.setUnidadOrigen(unidad);
            unidadGrupo.setActivo(true);
            unidadGrupo = unidadGrupoRepository.save(unidadGrupo);

            List<Tema> temas = temaRepository.findByUnidadId(unidad.getId())
                    .stream()
                    .filter(tema -> Boolean.TRUE.equals(tema.getActivo()))
                    .toList();

            for (Tema tema : temas) {
                TemaGrupo temaGrupo = new TemaGrupo();
                temaGrupo.setId(UUID.randomUUID().toString());
                temaGrupo.setNombre(tema.getNombre().trim());
                temaGrupo.setUnidadGrupo(unidadGrupo);
                temaGrupo.setTemaOrigen(tema);
                temaGrupo.setActivo(true);
                temaGrupo = temaGrupoRepository.save(temaGrupo);

                List<Concepto> conceptos = conceptoRepository.findByTemaId(tema.getId())
                        .stream()
                        .filter(concepto -> Boolean.TRUE.equals(concepto.getActivo()))
                        .toList();

                for (Concepto concepto : conceptos) {
                    ConceptoGrupo conceptoGrupo = new ConceptoGrupo();
                    conceptoGrupo.setId(UUID.randomUUID().toString());
                    conceptoGrupo.setNombre(concepto.getNombre().trim());
                    conceptoGrupo.setTemaGrupo(temaGrupo);
                    conceptoGrupo.setConceptoOrigen(concepto);
                    conceptoGrupo.setActivo(true);
                    conceptoGrupoRepository.save(conceptoGrupo);
                }
            }
        }
    }
}
