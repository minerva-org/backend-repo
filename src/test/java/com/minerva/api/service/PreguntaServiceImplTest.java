package com.minerva.api.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.model.Opcion;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.OpcionRepository;
import com.minerva.api.repository.PreguntaRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;
import com.minerva.api.repository.ConceptoGrupoRepository;
import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.ConceptoGrupo;
import com.minerva.api.model.TemaGrupo;
import com.minerva.api.model.UnidadGrupo;
import com.minerva.api.model.Grupo;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PreguntaServiceImplTest {

    @Mock
    private PreguntaRepository preguntaRepository;

    @Mock
    private ConceptoRepository conceptoRepository;

    @Mock
    private OpcionRepository opcionRepository;

    @Mock
    private QuizXPreguntaRepository quizXPreguntaRepository;

    @Mock
    private ConceptoGrupoRepository conceptoGrupoRepository;

    @InjectMocks
    private PreguntaServiceImpl preguntaService;

    @Test
    void deletePregunta_shouldDeleteRelatedOptionsAndQuizAssignments() {
        Pregunta pregunta = new Pregunta();
        pregunta.setId("P-1");

        Opcion opcion = new Opcion();
        opcion.setId("O-1");
        opcion.setPregunta(pregunta);

        QuizXPregunta quizXPregunta = new QuizXPregunta();
        quizXPregunta.setId("QXP-1");
        quizXPregunta.setPregunta(pregunta);

        when(preguntaRepository.findById("P-1")).thenReturn(Optional.of(pregunta));
        when(opcionRepository.findByPreguntaId("P-1")).thenReturn(List.of(opcion));
        when(quizXPreguntaRepository.findByPreguntaId("P-1")).thenReturn(List.of(quizXPregunta));

        preguntaService.deletePregunta("P-1");

        verify(opcionRepository).deleteAll(List.of(opcion));
        verify(quizXPreguntaRepository).deleteAll(List.of(quizXPregunta));
        verify(preguntaRepository).delete(pregunta);
    }

    // Input: CG-1 válido pertenece a G-1 y apunta a CON-1.
    // Resultado esperado: guarda P-1 con concepto base CON-1.
    @Test
    void savePregunta_shouldResolveConceptoGrupoFromCorrectGroup() {
        Concepto base = new Concepto();
        base.setId("CON-1");
        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        UnidadGrupo unidad = new UnidadGrupo();
        unidad.setGrupo(grupo);
        TemaGrupo tema = new TemaGrupo();
        tema.setUnidadGrupo(unidad);
        ConceptoGrupo conceptoGrupo = new ConceptoGrupo();
        conceptoGrupo.setId("CG-1");
        conceptoGrupo.setTemaGrupo(tema);
        conceptoGrupo.setConceptoOrigen(base);

        when(preguntaRepository.existsById("P-1")).thenReturn(false);
        when(conceptoGrupoRepository.findById("CG-1"))
                .thenReturn(Optional.of(conceptoGrupo));
        when(preguntaRepository.save(any(Pregunta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = com.minerva.api.dto.PreguntaDTO.builder()
                .id("P-1")
                .descripcion("Pregunta")
                .conceptoId("CG-1")
                .grupoId("G-1")
                .build();

        var result = preguntaService.savePregunta(dto);

        assertEquals("CON-1", result.getConceptoId());
    }

    // Input: concepto de grupo CG-1 perteneciente al grupo G-1.
    // Resultado esperado: resuelve su Concepto base y guarda la pregunta.
    @Test
    void savePregunta_shouldResolveConceptoGrupoToBaseConcept() {
        Concepto base = new Concepto();
        base.setId("CON-1");

        Grupo grupo = new Grupo();
        grupo.setId("G-1");

        UnidadGrupo unidadGrupo = new UnidadGrupo();
        unidadGrupo.setGrupo(grupo);
        TemaGrupo temaGrupo = new TemaGrupo();
        temaGrupo.setUnidadGrupo(unidadGrupo);
        ConceptoGrupo conceptoGrupo = new ConceptoGrupo();
        conceptoGrupo.setId("CG-1");
        conceptoGrupo.setTemaGrupo(temaGrupo);
        conceptoGrupo.setConceptoOrigen(base);

        when(conceptoGrupoRepository.findById("CG-1"))
                .thenReturn(Optional.of(conceptoGrupo));
        when(preguntaRepository.existsById("P-1")).thenReturn(false);
        when(preguntaRepository.save(any(Pregunta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PreguntaDTO dto = PreguntaDTO.builder()
                .id("P-1")
                .descripcion("Pregunta")
                .conceptoId("CG-1")
                .grupoId("G-1")
                .build();

        PreguntaDTO result = preguntaService.savePregunta(dto);

        assertEquals("CON-1", result.getConceptoId());
    }

    // Input: concepto CG-1 pertenece al grupo G-2 pero se usa en G-1.
    // Resultado esperado: 403 y no guarda pregunta.
    @Test
    void savePregunta_shouldRejectConceptoGrupoFromOtherGroup() {
        Grupo grupoConcepto = new Grupo();
        grupoConcepto.setId("G-2");
        UnidadGrupo unidadGrupo = new UnidadGrupo();
        unidadGrupo.setGrupo(grupoConcepto);
        TemaGrupo temaGrupo = new TemaGrupo();
        temaGrupo.setUnidadGrupo(unidadGrupo);
        ConceptoGrupo conceptoGrupo = new ConceptoGrupo();
        conceptoGrupo.setId("CG-1");
        conceptoGrupo.setTemaGrupo(temaGrupo);

        when(conceptoGrupoRepository.findById("CG-1"))
                .thenReturn(Optional.of(conceptoGrupo));

        PreguntaDTO dto = PreguntaDTO.builder()
                .id("P-1")
                .descripcion("Pregunta")
                .conceptoId("CG-1")
                .grupoId("G-1")
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> preguntaService.savePregunta(dto));

        assertEquals(403, error.getStatusCode().value());
        verify(preguntaRepository, never()).save(any());
    }

    // Input: concepto base CON-1 usado con G-1.
    // Resultado esperado: acepta solo si CON-1 pertenece al currículo del grupo.
    @Test
    void savePregunta_shouldRejectBaseConceptNotInGroupCurriculum() {
        when(preguntaRepository.existsById("P-1")).thenReturn(false);
        when(conceptoRepository.findById("CON-1"))
                .thenReturn(Optional.of(new Concepto()));
        when(conceptoGrupoRepository
                .existsByConceptoOrigenIdAndTemaGrupoUnidadGrupoGrupoId(
                        "CON-1", "G-1"))
                .thenReturn(false);

        PreguntaDTO dto = PreguntaDTO.builder()
                .id("P-1")
                .descripcion("Pregunta")
                .conceptoId("CON-1")
                .grupoId("G-1")
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> preguntaService.savePregunta(dto));

        assertEquals(403, error.getStatusCode().value());
        verify(preguntaRepository, never()).save(any());
    }
}
