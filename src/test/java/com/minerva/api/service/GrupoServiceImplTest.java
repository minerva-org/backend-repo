package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

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
import com.minerva.api.User.User;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GrupoServiceImplTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PlantelRepository plantelRepository;

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private CurriculoInicialGrupoService curriculoInicialGrupoService;

    @InjectMocks
    private GrupoServiceImpl grupoService;

    @Test
    void saveGrupo_shouldAllowCoordinatorAsTeacher() {
        Persona docente = new Persona();
        docente.setId("P-1");
        docente.setRol(Roles.COORDINADOR);

        Plantel plantel = new Plantel();
        plantel.setId(10L);

        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setNombre("Matemáticas");
        materia.setActivo(true);

        Persona alumno = new Persona();
        alumno.setId("A-1");
        alumno.setRol(Roles.ALUMNO);

        when(grupoRepository.existsById("G-1")).thenReturn(false);
        when(grupoRepository.existsByClaveGrupoIgnoreCase("G-101")).thenReturn(false);
        when(personaRepository.findById("P-1")).thenReturn(Optional.of(docente));
        when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno));
        when(plantelRepository.findById(10L)).thenReturn(Optional.of(plantel));
        when(materiaRepository.findById("MAT-1")).thenReturn(Optional.of(materia));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> {
            Grupo saved = invocation.getArgument(0);
            assertEquals(1, saved.getAlumnos().size());
            assertTrue(saved.getAlumnos().stream().anyMatch(a -> "A-1".equals(a.getId())));
            return saved;
        });

        GrupoDTO dto = GrupoDTO.builder()
            .id("G-1")
            .claveGrupo("G-101")
            .nombre("Matemáticas")
            .semestre("2025-A")
            .docenteId("P-1")
            .plantelId(10L)
            .materiaId("MAT-1")
            .alumnosIds(List.of("A-1"))
            .build();

        GrupoDTO result = grupoService.saveGrupo(dto, null);

        assertNotNull(result);
        assertEquals("P-1", result.getDocenteId());
        assertTrue(result.getAlumnosIds() != null && result.getAlumnosIds().contains("A-1"));
        verify(curriculoInicialGrupoService).initializeFromMateria(any(Grupo.class), any(Materia.class));
    }

    // Input: grupo válido pero la copia inicial falla por curriculo incompleto.
    // Resultado esperado: propaga el error y no confirma el grupo.
    @Test
    void saveGrupo_shouldRollbackWhenInitialCurriculumFails() {
        Persona docente = new Persona();
        docente.setId("P-1");
        docente.setRol(Roles.DOCENTE);

        Plantel plantel = new Plantel();
        plantel.setId(10L);

        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setNombre("Matemáticas");
        materia.setActivo(true);

        when(grupoRepository.existsById("G-ROLLBACK")).thenReturn(false);
        when(grupoRepository.existsByClaveGrupoIgnoreCase("G-ROLLBACK")).thenReturn(false);
        when(materiaRepository.findById("MAT-1")).thenReturn(Optional.of(materia));
        when(personaRepository.findById("P-1")).thenReturn(Optional.of(docente));
        when(plantelRepository.findById(10L)).thenReturn(Optional.of(plantel));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalArgumentException("La materia no tiene curriculo inicial con unidades."))
                .when(curriculoInicialGrupoService)
                .initializeFromMateria(any(Grupo.class), any(Materia.class));

        GrupoDTO dto = GrupoDTO.builder()
                .id("G-ROLLBACK")
                .claveGrupo("G-ROLLBACK")
                .nombre("Rollback")
                .semestre("2026-A")
                .docenteId("P-1")
                .plantelId(10L)
                .materiaId("MAT-1")
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> grupoService.saveGrupo(dto, null));

        assertTrue(error.getMessage().contains("curriculo"));
    }

    @Test
    void findAllByAlumnoId_shouldReturnGroupsForAlumno() {
        Persona alumno = new Persona();
        alumno.setId("A-1");
        alumno.setRol(Roles.ALUMNO);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setNombre("Matemáticas");
        grupo.setMateria(materia);
        grupo.addAlumno(alumno);

        when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno));
        when(grupoRepository.findByAlumnos_Id("A-1")).thenReturn(List.of(grupo));

        var result = grupoService.findAllByAlumnoId("A-1");

        assertEquals(1, result.size());
        assertEquals("G-1", result.get(0).getId());
        assertTrue(result.get(0).getAlumnosIds().contains("A-1"));
    }

    // Input: materia null en el DTO.
    // Resultado esperado: excepción descriptiva y no consulta materia.
    @Test
    void saveGrupo_shouldRejectNullMateria() {
        GrupoDTO dto = GrupoDTO.builder()
                .id("G-1")
                .claveGrupo("G-101")
                .nombre("Grupo")
                .semestre("2026-A")
                .plantelId(10L)
                .materiaId(null)
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> grupoService.saveGrupo(dto, null));

        assertTrue(error.getMessage().contains("materia"));
        verify(materiaRepository, never()).findById(any());
    }

    // Input: materia desactivada.
    // Resultado esperado: excepción descriptiva y no inicia currículo.
    @Test
    void saveGrupo_shouldRejectInactiveMateria() {
        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setNombre("Matemáticas");
        materia.setActivo(false);

        when(materiaRepository.findById("MAT-1")).thenReturn(Optional.of(materia));

        GrupoDTO dto = GrupoDTO.builder()
                .id("G-1")
                .claveGrupo("G-101")
                .nombre("Grupo")
                .semestre("2026-A")
                .plantelId(10L)
                .materiaId("MAT-1")
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> grupoService.saveGrupo(dto, null));

        assertTrue(error.getMessage().contains("desactivada"));
        verify(curriculoInicialGrupoService, never())
                .initializeFromMateria(any(Grupo.class), any(Materia.class));
    }

    // Input: Director de Plantel intenta crear grupo en plantel 20 desde plantel 10.
    // Resultado esperado: 403 y no persiste.
    @Test
    void saveGrupo_shouldRejectDirectorFromOtherPlantel() {
        Persona director = new Persona();
        director.setRol(Roles.DIRECTOR_PLANTEL);
        Plantel plantelDirector = new Plantel();
        plantelDirector.setId(10L);
        director.setPlantel(plantelDirector);
        User usuario = new User();
        usuario.setPersona(director);

        GrupoDTO dto = GrupoDTO.builder()
                .id("G-1")
                .claveGrupo("G-101")
                .nombre("Grupo")
                .semestre("2026-A")
                .plantelId(20L)
                .materiaId("MAT-1")
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> grupoService.saveGrupo(dto, usuario));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("plantel"));
        verify(grupoRepository, never()).save(any());
    }

    // Input: usuario sin rol admitido.
    // Resultado esperado: 403 antes de consultar materia.
    @Test
    void saveGrupo_shouldRejectUserWithoutCatalogPermission() {
        Persona alumno = new Persona();
        alumno.setRol(Roles.ALUMNO);
        User usuario = new User();
        usuario.setPersona(alumno);

        GrupoDTO dto = GrupoDTO.builder()
                .id("G-1")
                .claveGrupo("G-101")
                .nombre("Grupo")
                .semestre("2026-A")
                .plantelId(10L)
                .materiaId("MAT-1")
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> grupoService.saveGrupo(dto, usuario));

        assertEquals(403, error.getStatusCode().value());
        verify(materiaRepository, never()).findById(any());
    }

    // Input: nombre, clave o semestre en blanco.
    // Resultado esperado: excepción descriptiva y no persiste.
    @Test
    void saveGrupo_shouldRejectBlankRequiredFields() {
        for (GrupoDTO dto : List.of(
                GrupoDTO.builder().id("G-1").claveGrupo(" ").nombre("Grupo").semestre("2026-A").plantelId(10L).materiaId("MAT-1").build(),
                GrupoDTO.builder().id("G-1").claveGrupo("G-101").nombre(" ").semestre("2026-A").plantelId(10L).materiaId("MAT-1").build(),
                GrupoDTO.builder().id("G-1").claveGrupo("G-101").nombre("Grupo").semestre(" ").plantelId(10L).materiaId("MAT-1").build())) {
            assertThrows(IllegalArgumentException.class,
                    () -> grupoService.saveGrupo(dto, null));
        }
        verify(grupoRepository, never()).save(any());
    }

    // Input: materia específica del plantel 20 usada en grupo del plantel 10.
    // Resultado esperado: 403 y no persiste.
    @Test
    void saveGrupo_shouldRejectMateriaFromOtherPlantel() {
        Materia materia = new Materia();
        materia.setId("MAT-2");
        materia.setNombre("Materia Norte");
        materia.setActivo(true);
        materia.setPlantelId(20L);

        when(materiaRepository.findById("MAT-2")).thenReturn(Optional.of(materia));
        when(plantelRepository.findById(10L)).thenReturn(Optional.of(new Plantel()));

        GrupoDTO dto = GrupoDTO.builder()
                .id("G-2")
                .claveGrupo("G-102")
                .nombre("Grupo Centro")
                .semestre("2026-A")
                .plantelId(10L)
                .materiaId("MAT-2")
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> grupoService.saveGrupo(dto, null));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("plantel"));
        verify(grupoRepository, never()).save(any());
    }

    // Input: docente pertenece al plantel 20 y el grupo se crea en plantel 10.
    // Resultado esperado: 403 y no persiste.
    @Test
    void saveGrupo_shouldRejectDocenteFromOtherPlantel() {
        Materia materia = new Materia();
        materia.setId("MAT-1");
        materia.setNombre("Matemáticas");
        materia.setActivo(true);

        Plantel plantelNorte = new Plantel();
        plantelNorte.setId(20L);

        Persona docente = new Persona();
        docente.setId("P-1");
        docente.setRol(Roles.DOCENTE);
        docente.setPlantel(plantelNorte);

        when(materiaRepository.findById("MAT-1")).thenReturn(Optional.of(materia));
        when(personaRepository.findById("P-1")).thenReturn(Optional.of(docente));

        GrupoDTO dto = GrupoDTO.builder()
                .id("G-1")
                .claveGrupo("G-101")
                .nombre("Grupo")
                .semestre("2026-A")
                .docenteId("P-1")
                .plantelId(10L)
                .materiaId("MAT-1")
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> grupoService.saveGrupo(dto, null));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("docente"));
        verify(grupoRepository, never()).save(any());
    }
}
