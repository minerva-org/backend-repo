package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.User.Roles;
import com.minerva.api.dto.GrupoDTO;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.PlantelRepository;

@ExtendWith(MockitoExtension.class)
class GrupoServiceImplTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PlantelRepository plantelRepository;

    @InjectMocks
    private GrupoServiceImpl grupoService;

    @Test
    void saveGrupo_shouldAllowCoordinatorAsTeacher() {
        Persona docente = new Persona();
        docente.setId("P-1");
        docente.setRol(Roles.COORDINADOR);

        Plantel plantel = new Plantel();
        plantel.setId(10L);

        Persona alumno = new Persona();
        alumno.setId("A-1");
        alumno.setRol(Roles.ALUMNO);

        when(grupoRepository.existsById("G-1")).thenReturn(false);
        when(grupoRepository.existsByClaveGrupoIgnoreCase("G-101")).thenReturn(false);
        when(personaRepository.findById("P-1")).thenReturn(Optional.of(docente));
        when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno));
        when(plantelRepository.findById(10L)).thenReturn(Optional.of(plantel));
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
            .alumnosIds(java.util.List.of("A-1"))
            .build();

        GrupoDTO result = grupoService.saveGrupo(dto);

        assertNotNull(result);
        assertEquals("P-1", result.getDocenteId());
        assertTrue(result.getAlumnosIds() != null && result.getAlumnosIds().contains("A-1"));
    }

    @Test
    void findAllByAlumnoId_shouldReturnGroupsForAlumno() {
        Persona alumno = new Persona();
        alumno.setId("A-1");
        alumno.setRol(Roles.ALUMNO);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.addAlumno(alumno);

        when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno));
        when(grupoRepository.findDistinctByAlumnosId("A-1")).thenReturn(java.util.List.of(grupo));

        var result = grupoService.findAllByAlumnoId("A-1");

        assertEquals(1, result.size());
        assertEquals("G-1", result.get(0).getId());
        assertTrue(result.get(0).getAlumnosIds().contains("A-1"));
    }
}
