package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.User.Roles;
import com.minerva.api.dto.GrupoDTO;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.MateriaRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.PlantelRepository;

@ExtendWith(MockitoExtension.class)
class GrupoServiceImplTest {

    private static final Long PLANTEL_ID = 10L;

    @Mock private GrupoRepository grupoRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private PlantelRepository plantelRepository;
    @Mock private MateriaRepository materiaRepository;
    @Mock private MateriaService materiaService;

    @InjectMocks
    private GrupoServiceImpl grupoService;

    @Test
    void saveGrupo_shouldAllowCoordinatorAsTeacher_withGlobalMateria() {
        stubGrupoNuevo();
        Materia global = materia("MAT-001", null);   // plantelId null = global
        when(materiaRepository.findById("MAT-001")).thenReturn(Optional.of(global));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> {
            Grupo saved = invocation.getArgument(0);
            assertEquals(1, saved.getAlumnos().size());
            assertTrue(saved.getAlumnos().stream().anyMatch(a -> "A-1".equals(a.getId())));
            return saved;
        });

        GrupoDTO result = grupoService.saveGrupo(grupoDTO("MAT-001"));

        assertNotNull(result);
        assertEquals("P-1", result.getDocenteId());
        assertTrue(result.getAlumnosIds() != null && result.getAlumnosIds().contains("A-1"));
    }

    @Test
    void saveGrupo_shouldAllowMateriaPropiaDelMismoPlantel() {
        stubGrupoNuevo();
        Materia propia = materia("MAT-010", PLANTEL_ID);
        propia.setMateriaBaseId("MAT-001");   // es la versión del plantel de una global
        when(materiaRepository.findById("MAT-010")).thenReturn(Optional.of(propia));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(i -> i.getArgument(0));

        GrupoDTO result = grupoService.saveGrupo(grupoDTO("MAT-010"));

        assertNotNull(result);
    }

    @Test
    void saveGrupo_shouldRejectMateriaDeOtroPlantel() {
        Materia ajena = materia("MAT-099", 99L);
        when(grupoRepository.existsById("G-1")).thenReturn(false);
        when(materiaRepository.findById("MAT-099")).thenReturn(Optional.of(ajena));
        lenient().when(grupoRepository.existsByClaveGrupoIgnoreCase("G-101")).thenReturn(false);
        lenient().when(personaRepository.findById("P-1")).thenReturn(Optional.of(docente()));
        lenient().when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno()));
        lenient().when(plantelRepository.findById(PLANTEL_ID)).thenReturn(Optional.of(plantel()));

        assertThrows(IllegalArgumentException.class,
                () -> grupoService.saveGrupo(grupoDTO("MAT-099")));
        verify(grupoRepository, never()).save(any(Grupo.class));
    }

    @Test
    void findAllByAlumnoId_shouldReturnGroupsForAlumno() {
        Persona alumno = alumno();

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.addAlumno(alumno);
        grupo.setMateria(materia("MAT-001", null));

        when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno));
        when(grupoRepository.findByAlumnos_Id("A-1")).thenReturn(List.of(grupo));

        var results = grupoService.findAllByAlumnoId("A-1");

        assertEquals(1, results.size());
        assertEquals("G-1", results.get(0).getId());
        assertTrue(results.get(0).getAlumnosIds().contains("A-1"));
    }

    private void stubGrupoNuevo() {
        when(grupoRepository.existsById("G-1")).thenReturn(false);
        when(grupoRepository.existsByClaveGrupoIgnoreCase("G-101")).thenReturn(false);
        when(personaRepository.findById("P-1")).thenReturn(Optional.of(docente()));
        when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno()));
        when(plantelRepository.findById(PLANTEL_ID)).thenReturn(Optional.of(plantel()));
    }

    private GrupoDTO grupoDTO(String materiaId) {
        return GrupoDTO.builder()
                .id("G-1")
                .claveGrupo("G-101")
                .nombre("Matemáticas")
                .materiaId(materiaId)
                .semestre("2025-A")
                .docenteId("P-1")
                .plantelId(PLANTEL_ID)
                .alumnosIds(List.of("A-1"))
                .build();
    }

    private Materia materia(String id, Long plantelId) {
        Materia m = new Materia();
        m.setId(id);
        m.setNombre("Matemáticas");
        m.setPrefijo("MATE");
        m.setPlanEstudio(null);
        m.setPlantelId(plantelId);
        m.setActivo(true);
        return m;
    }

    private Persona docente() {
        Persona p = new Persona();
        p.setId("P-1");
        p.setRol(Roles.COORDINADOR);
        return p;
    }

    private Persona alumno() {
        Persona p = new Persona();
        p.setId("A-1");
        p.setRol(Roles.ALUMNO);
        return p;
    }

    private Plantel plantel() {
        Plantel p = new Plantel();
        p.setId(PLANTEL_ID);
        return p;
    }
}