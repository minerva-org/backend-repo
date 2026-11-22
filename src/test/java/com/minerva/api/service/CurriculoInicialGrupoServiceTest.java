package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.model.Concepto;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Tema;
import com.minerva.api.model.Unidad;
import com.minerva.api.repository.ConceptoGrupoRepository;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.TemaGrupoRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadGrupoRepository;
import com.minerva.api.repository.UnidadRepository;

@ExtendWith(MockitoExtension.class)
class CurriculoInicialGrupoServiceTest {

    @Mock private UnidadRepository unidadRepository;
    @Mock private TemaRepository temaRepository;
    @Mock private ConceptoRepository conceptoRepository;
    @Mock private UnidadGrupoRepository unidadGrupoRepository;
    @Mock private TemaGrupoRepository temaGrupoRepository;
    @Mock private ConceptoGrupoRepository conceptoGrupoRepository;

    @InjectMocks
    private CurriculoInicialGrupoService service;

    // Input: grupo válido y materia con una unidad activa, un tema activo y un concepto activo.
    // Resultado esperado: crea 1 UnidadGrupo, 1 TemaGrupo y 1 ConceptoGrupo con relaciones origen.
    @Test
    void initializeFromMateria_shouldCopyCompleteCurriculum() {
        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setActivo(true);

        Unidad unidad = new Unidad();
        unidad.setId("UNI-1");
        unidad.setNombre("Unidad base");
        unidad.setMateria(materia);
        unidad.setActivo(true);

        Tema tema = new Tema();
        tema.setId("TEM-1");
        tema.setNombre("Tema base");
        tema.setUnidad(unidad);
        tema.setActivo(true);

        Concepto concepto = new Concepto();
        concepto.setId("CON-1");
        concepto.setNombre("Concepto base");
        concepto.setTema(tema);
        concepto.setActivo(true);

        Grupo grupo = new Grupo();
        grupo.setId("GRP-1");
        grupo.setMateria(materia);

        when(unidadRepository.findByMateriaId("MAT-1")).thenReturn(List.of(unidad));
        when(temaRepository.findByUnidadId("UNI-1")).thenReturn(List.of(tema));
        when(conceptoRepository.findByTemaId("TEM-1")).thenReturn(List.of(concepto));
        when(unidadGrupoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(temaGrupoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(conceptoGrupoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.initializeFromMateria(grupo, materia);

        verify(unidadGrupoRepository).save(any());
        verify(temaGrupoRepository).save(any());
        verify(conceptoGrupoRepository).save(any());
    }

    // Input: grupo null.
    // Resultado esperado: IllegalArgumentException descriptivo y sin consultas.
    @Test
    void initializeFromMateria_shouldRejectNullGroup() {
        Materia materia = new Materia();
        materia.setId("MAT-1");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeFromMateria(null, materia));

        assertTrue(error.getMessage().contains("grupo"));
        verify(unidadRepository, never()).findByMateriaId(any());
    }

    // Input: materia null.
    // Resultado esperado: IllegalArgumentException descriptivo y sin consultas.
    @Test
    void initializeFromMateria_shouldRejectNullMateria() {
        Grupo grupo = new Grupo();
        grupo.setId("GRP-1");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeFromMateria(grupo, null));

        assertTrue(error.getMessage().contains("materia"));
        verify(unidadRepository, never()).findByMateriaId(any());
    }

    // Input: materia sin unidades activas.
    // Resultado esperado: IllegalArgumentException; no guarda currículo parcial.
    @Test
    void initializeFromMateria_shouldRejectMateriaWithoutUnits() {
        Grupo grupo = new Grupo();
        grupo.setId("GRP-1");
        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setActivo(true);

        when(unidadRepository.findByMateriaId("MAT-1")).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeFromMateria(grupo, materia));

        assertTrue(error.getMessage().contains("curriculo"));
        verify(unidadGrupoRepository, never()).save(any());
    }

    // Input: unidad activa sin temas activos.
    // Resultado esperado: reject the incomplete curriculum before partial save.
    @Test
    void initializeFromMateria_shouldRejectUnitWithoutTopics() {
        Grupo grupo = new Grupo();
        grupo.setId("GRP-1");
        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setActivo(true);

        Unidad unidad = new Unidad();
        unidad.setId("UNI-1");
        unidad.setNombre("Unidad");
        unidad.setMateria(materia);
        unidad.setActivo(true);

        when(unidadRepository.findByMateriaId("MAT-1")).thenReturn(List.of(unidad));
        when(temaRepository.findByUnidadId("UNI-1")).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeFromMateria(grupo, materia));

        assertTrue(error.getMessage().contains("temas"));
        verify(unidadGrupoRepository, never()).save(any());
    }

    // Input: tema activo sin conceptos activos.
    // Resultado esperado: reject incomplete hierarchy before partial save.
    @Test
    void initializeFromMateria_shouldRejectTopicWithoutConcepts() {
        Grupo grupo = new Grupo();
        grupo.setId("GRP-1");
        Materia materia = new Materia();
        materia.setId("MAT-1");

        Unidad unidad = new Unidad();
        unidad.setId("UNI-1");
        unidad.setNombre("Unidad");
        unidad.setMateria(materia);
        unidad.setActivo(true);

        Tema tema = new Tema();
        tema.setId("TEM-1");
        tema.setNombre("Tema");
        tema.setUnidad(unidad);
        tema.setActivo(true);

        when(unidadRepository.findByMateriaId("MAT-1")).thenReturn(List.of(unidad));
        when(temaRepository.findByUnidadId("UNI-1")).thenReturn(List.of(tema));
        when(conceptoRepository.findByTemaId("TEM-1")).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeFromMateria(grupo, materia));

        assertTrue(error.getMessage().contains("conceptos"));
        verify(unidadGrupoRepository, never()).save(any());
    }
}
