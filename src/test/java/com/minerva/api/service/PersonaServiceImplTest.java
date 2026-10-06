package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
import com.minerva.api.User.UserRepository;
import com.minerva.api.dto.PersonaDTO;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.PlantelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PersonaServiceImplTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PlantelRepository plantelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonaServiceImpl personaService;

    // @Test
    // void findAllDocentesByPlantelId_shouldReturnDocentesAndCoordinadores() {
    //     Persona docente = new Persona();
    //     docente.setId("P-1");
    //     docente.setNombre("Ana");
    //     docente.setApellido("Lopez");
    //     docente.setRol(Roles.DOCENTE);

    //     Persona coordinador = new Persona();
    //     coordinador.setId("P-2");
    //     coordinador.setNombre("Carlos");
    //     coordinador.setApellido("Perez");
    //     coordinador.setRol(Roles.COORDINADOR);

    //     User user = new User();
    //     user.setUsername("ana.lopez");

    //     User user2 = new User();
    //     user2.setUsername("carlos.perez");

    //     when(plantelRepository.existsById(10L)).thenReturn(true);
    //     when(personaRepository.findByPlantelId(10L)).thenReturn(List.of(docente, coordinador));
    //     when(userRepository.findByPersonaId("P-1")).thenReturn(Optional.of(user));
    //     when(userRepository.findByPersonaId("P-2")).thenReturn(Optional.of(user2));

    //     List<PersonaDTO> result = personaService.findAllDocentesByPlantelId(10L);

    //     assertEquals(2, result.size());
    //     assertEquals("P-1", result.get(0).getId());
    //     assertEquals("ana.lopez", result.get(0).getUsername());
    //     assertEquals("P-2", result.get(1).getId());
    //     assertEquals("carlos.perez", result.get(1).getUsername());
    // }

    // @Test
    // void findAllByPlantelIdAndRol_shouldReturnAlumnos() {
    //     Persona alumno = new Persona();
    //     alumno.setId("A-1");
    //     alumno.setNombre("Luis");
    //     alumno.setApellido("Perez");
    //     alumno.setRol(Roles.ALUMNO);

    //     User user = new User();
    //     user.setUsername("luis.perez");

    //     when(plantelRepository.existsById(10L)).thenReturn(true);
    //     when(personaRepository.findByPlantelIdAndRol(10L, Roles.ALUMNO)).thenReturn(List.of(alumno));
    //     when(userRepository.findByPersonaId("A-1")).thenReturn(Optional.of(user));

    //     List<PersonaDTO> result = personaService.findAllByPlantelIdAndRol(10L, Roles.ALUMNO);

    //     assertEquals(1, result.size());
    //     assertEquals("A-1", result.get(0).getId());
    //     assertTrue(result.get(0).getUsername().equals("luis.perez"));
    // }

    // @Test
    // void savePersona_shouldUseProvidedPasswordForDocente() {
    //     PersonaDTO dto = PersonaDTO.builder()
    //         .id("P-1")
    //         .nombre("Ana")
    //         .apellido("Lopez")
    //         .email("ana.lopez@minerva.local")
    //         .password("Secreto123")
    //         .rol(Roles.DOCENTE)
    //         .plantelId(10L)
    //         .build();

    //     Plantel plantel = new Plantel();
    //     plantel.setId(10L);

    //     when(personaRepository.existsById("P-1")).thenReturn(false);
    //     when(personaRepository.existsByEmailIgnoreCase("ana.lopez@minerva.local")).thenReturn(false);
    //     when(plantelRepository.findById(10L)).thenReturn(Optional.of(plantel));
    //     when(userRepository.existsByUsernameIgnoreCase("ana.lopez")).thenReturn(false);
    //     when(passwordEncoder.encode("Secreto123")).thenReturn("encoded-password");
    //     when(personaRepository.save(any(Persona.class))).thenAnswer(invocation -> invocation.getArgument(0));

    //     PersonaDTO result = personaService.savePersona(dto);

    //     assertEquals("P-1", result.getId());
    //     verify(passwordEncoder).encode("Secreto123");
    // }

    // @Test
    // void savePersona_shouldUseProvidedPasswordForAlumno() {
    //     PersonaDTO dto = PersonaDTO.builder()
    //         .id("A-1")
    //         .nombre("Luis")
    //         .apellido("Perez")
    //         .email("luis.perez@minerva.local")
    //         .password("Alumno123")
    //         .rol(Roles.ALUMNO)
    //         .plantelId(10L)
    //         .build();

    //     Plantel plantel = new Plantel();
    //     plantel.setId(10L);

    //     when(personaRepository.existsById("A-1")).thenReturn(false);
    //     when(personaRepository.existsByEmailIgnoreCase("luis.perez@minerva.local")).thenReturn(false);
    //     when(plantelRepository.findById(10L)).thenReturn(Optional.of(plantel));
    //     when(userRepository.existsByUsernameIgnoreCase("luis.perez")).thenReturn(false);
    //     when(passwordEncoder.encode("Alumno123")).thenReturn("encoded-alumno-password");
    //     when(personaRepository.save(any(Persona.class))).thenAnswer(invocation -> invocation.getArgument(0));

    //     PersonaDTO result = personaService.savePersona(dto);

    //     assertEquals("A-1", result.getId());
    //     verify(passwordEncoder).encode("Alumno123");
    // }
}