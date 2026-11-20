package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.dto.ConceptoDTO;
import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.dto.TemaDTO;
import com.minerva.api.dto.UnidadDTO;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Tema;
import com.minerva.api.model.Unidad;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.MateriaRepository;
import com.minerva.api.repository.PlanEstudioRepository;
import com.minerva.api.repository.TemaRepository;
import com.minerva.api.repository.UnidadRepository;
import com.minerva.api.repository.DocumentoRepository;
import com.minerva.api.repository.PlantelRepository;
import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class MateriaServiceImplTest {

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private PlantelRepository plantelRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private PlanEstudioRepository planEstudioRepository;

    @Mock
    private UnidadRepository unidadRepository;

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private ConceptoRepository conceptoRepository;

    @InjectMocks
    private MateriaServiceImpl materiaService;

    @Test
    void saveMateria_shouldAllowMissingPlanEstudioUntilThatFeatureIsImplemented() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-001")
            .nombre("Matemáticas")
            .prefijo("MATE")
            .planEstudioId(null)
            .build();

        when(materiaRepository.existsById("MAT-001")).thenReturn(false);
        when(materiaRepository.existsByNombreIgnoreCaseAndPlantelIdIsNull("Matemáticas")).thenReturn(false);
        when(unidadRepository.findByMateriaId("MAT-001")).thenReturn(Collections.emptyList());
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> {
            Materia materia = invocation.getArgument(0);
            materia.setId("MAT-001");
            return materia;
        });

        MateriaDTO result = materiaService.saveMateria(dto, null);

        assertNotNull(result);
        assertEquals("MAT-001", result.getId());
        verify(planEstudioRepository, never()).findById(any());
    }

    @Test
    void saveMateria_shouldPersistNestedUnidadesTemasYConceptos() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-002")
            .nombre("Historia")
            .prefijo("HIST")
            .planEstudioId(null)
            .unidades(List.of(
                UnidadDTO.builder()
                    .id("UNI-1")
                    .nombre("Revolución")
                    .temas(List.of(
                        TemaDTO.builder()
                            .id("TEM-1")
                            .nombre("Causas")
                            .conceptos(List.of(
                                ConceptoDTO.builder().id("CON-1").nombre("Industrialización").build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .build();

        when(materiaRepository.existsById("MAT-002")).thenReturn(false);
        when(materiaRepository.existsByNombreIgnoreCaseAndPlantelIdIsNull("Historia")).thenReturn(false);
        when(unidadRepository.save(any(Unidad.class))).thenAnswer(invocation -> {
            Unidad unidad = invocation.getArgument(0);
            unidad.setId("UNI-1");
            return unidad;
        });
        when(temaRepository.save(any(Tema.class))).thenAnswer(invocation -> {
            Tema tema = invocation.getArgument(0);
            tema.setId("TEM-1");
            return tema;
        });
        when(conceptoRepository.save(any(Concepto.class))).thenAnswer(invocation -> {
            Concepto concepto = invocation.getArgument(0);
            concepto.setId("CON-1");
            return concepto;
        });
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> {
            Materia materia = invocation.getArgument(0);
            materia.setId("MAT-002");
            return materia;
        });

        Unidad unidadPersistida = new Unidad();
        unidadPersistida.setId("UNI-1");
        unidadPersistida.setNombre("Revolución");
        Tema temaPersistido = new Tema();
        temaPersistido.setId("TEM-1");
        temaPersistido.setNombre("Causas");
        Concepto conceptoPersistido = new Concepto();
        conceptoPersistido.setId("CON-1");
        conceptoPersistido.setNombre("Industrialización");

        when(unidadRepository.findByMateriaId("MAT-002")).thenReturn(List.of(unidadPersistida));
        when(temaRepository.findByUnidadId("UNI-1")).thenReturn(List.of(temaPersistido));
        when(conceptoRepository.findByTemaId("TEM-1")).thenReturn(List.of(conceptoPersistido));

        MateriaDTO result = materiaService.saveMateria(dto, null);

        assertNotNull(result);
        assertEquals("MAT-002", result.getId());
        assertEquals(1, result.getUnidades().size());
        assertEquals("UNI-1", result.getUnidades().get(0).getId());
        assertEquals(1, result.getUnidades().get(0).getTemas().size());
        assertEquals("CON-1", result.getUnidades().get(0).getTemas().get(0).getConceptos().get(0).getId());
    }

    private User usuario(Roles rol, Long plantelId) {
        Persona persona = new Persona();
        persona.setRol(rol);
        if (plantelId != null) {
            Plantel plantel = new Plantel();
            plantel.setId(plantelId);
            persona.setPlantel(plantel);
        }
        User usuario = new User();
        usuario.setPersona(persona);
        return usuario;
    }

    // Input: Director de Plantel sin id de plantel solicitado en DTO.
    // Resultado esperado: fuerza el plantel propio y persiste la materia.
    @Test
    void saveMateria_shouldUseDirectorPlantelCampus() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-CAMPUS")
            .nombre("Álgebra")
            .prefijo("ALG1")
            .plantelId(null)
            .build();
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);

        when(materiaRepository.existsById("MAT-CAMPUS")).thenReturn(false);
        when(materiaRepository.existsByNombreIgnoreCaseAndPlantelId("Álgebra", "7")).thenReturn(false);
        when(plantelRepository.existsById(7L)).thenReturn(true);
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MateriaDTO result = materiaService.saveMateria(dto, director);

        assertEquals(7L, dto.getPlantelId());
        assertEquals(7L, result.getPlantelId());
    }

    // Input: Director de Plantel solicita crear en plantelId=8.
    // Resultado esperado: HTTP 403 y no persiste la materia.
    @Test
    void saveMateria_shouldRejectDirectorCreatingInOtherCampus() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-CAMPUS")
            .nombre("Álgebra")
            .prefijo("ALG1")
            .plantelId(8L)
            .build();
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.saveMateria(dto, director));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("tu plantel"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: Coordinador sin plantel asignado.
    // Resultado esperado: HTTP 403 y no persiste la materia.
    @Test
    void saveMateria_shouldRejectCampusUserWithoutPlantel() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-CAMPUS")
            .nombre("Álgebra")
            .prefijo("ALG1")
            .build();
        User coordinador = usuario(Roles.COORDINADOR, null);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.saveMateria(dto, coordinador));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("plantel asignado"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: Usuario DOCENTE intenta crear una materia.
    // Resultado esperado: HTTP 403 antes de validar o persistir datos.
    @Test
    void saveMateria_shouldRejectUserWithoutCatalogPermission() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-DOCENTE")
            .nombre("Química")
            .prefijo("QUI1")
            .build();
        User docente = usuario(Roles.DOCENTE, 7L);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.saveMateria(dto, docente));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("administrar materias"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: nombre vacío y usuario Director General.
    // Resultado esperado: IllegalArgumentException descriptivo.
    @Test
    void saveMateria_shouldRejectBlankName() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-BLANK")
            .nombre("   ")
            .prefijo("BLN1")
            .build();
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> materiaService.saveMateria(dto, director));

        assertTrue(error.getMessage().contains("nombre"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: ID null y usuario Director General.
    // Resultado esperado: error descriptivo y sin persistencia.
    @Test
    void saveMateria_shouldRejectNullId() {
        MateriaDTO dto = MateriaDTO.builder()
            .nombre("Física")
            .prefijo("FIS1")
            .build();
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> materiaService.saveMateria(dto, director));

        assertTrue(error.getMessage().contains("id"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: DTO null y usuario Director General.
    // Resultado esperado: IllegalArgumentException descriptivo y no acceso a repositorios.
    @Test
    void saveMateria_shouldRejectNullDto() {
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> materiaService.saveMateria(null, director));

        assertTrue(error.getMessage().contains("datos"));
        verify(materiaRepository, never()).existsById(any());
    }

    // Input: nombre y prefijo de exactamente 255 caracteres.
    // Resultado esperado: acepta la longitud límite y persiste.
    @Test
    void saveMateria_shouldAcceptMaximumLengths() {
        String nombre = "N".repeat(255);
        String prefijo = "P".repeat(255);
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-LIMIT")
            .nombre(nombre)
            .prefijo(prefijo)
            .plantelId(null)
            .build();
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        when(materiaRepository.existsById("MAT-LIMIT")).thenReturn(false);
        when(materiaRepository.existsByNombreIgnoreCaseAndPlantelIdIsNull(nombre)).thenReturn(false);
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MateriaDTO result = materiaService.saveMateria(dto, director);

        assertEquals(nombre, result.getNombre());
        assertEquals(prefijo, result.getPrefijo());
    }

    // Input: nombre de 256 caracteres.
    // Resultado esperado: IllegalArgumentException descriptivo sin persistencia.
    @Test
    void saveMateria_shouldRejectNameOver255Characters() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-LONG")
            .nombre("N".repeat(256))
            .prefijo("MAX1")
            .build();
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> materiaService.saveMateria(dto, director));

        assertTrue(error.getMessage().contains("255"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: prefijo blanco.
    // Resultado esperado: IllegalArgumentException antes de persistir.
    @Test
    void saveMateria_shouldRejectBlankPrefix() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-PREFIX")
            .nombre("Materia")
            .prefijo("   ")
            .build();
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> materiaService.saveMateria(dto, director));

        assertTrue(error.getMessage().contains("prefijo"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: documento asociado inexistente.
    // Resultado esperado: EntityNotFoundException con el ID buscado.
    @Test
    void saveMateria_shouldRejectUnknownDocument() {
        MateriaDTO dto = MateriaDTO.builder()
            .id("MAT-DOC")
            .nombre("Biología")
            .prefijo("BIO1")
            .documentoMateriaId("DOC-404")
            .build();
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        when(materiaRepository.existsById("MAT-DOC")).thenReturn(false);
        when(materiaRepository.existsByNombreIgnoreCaseAndPlantelIdIsNull("Biología")).thenReturn(false);
        when(documentoRepository.findById("DOC-404")).thenReturn(Optional.empty());

        EntityNotFoundException error = assertThrows(
            EntityNotFoundException.class,
            () -> materiaService.saveMateria(dto, director));

        assertTrue(error.getMessage().contains("DOC-404"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: Director de Plantel intenta desactivar materia tronco común.
    // Resultado esperado: HTTP 403 y no cambia activo ni jerarquía.
    @Test
    void updateEstado_shouldRejectCommonCoreMateriaForCampusRole() {
        Materia materia = new Materia();
        materia.setId("MAT-COMMON");
        materia.setPlantelId(null);
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);
        when(materiaRepository.findById("MAT-COMMON")).thenReturn(Optional.of(materia));

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.updateEstado("MAT-COMMON", false, director));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("tronco común"));
        verify(materiaRepository, never()).save(any());
        verify(unidadRepository, never()).cambiarEstadoPorMateria(any(), anyBoolean());
    }

    // Input: Director de Plantel intenta desactivar materia de otro plantel.
    // Resultado esperado: HTTP 403 y sin persistencia.
    @Test
    void updateEstado_shouldRejectMateriaFromOtherCampus() {
        Materia materia = new Materia();
        materia.setId("MAT-CAMPUS");
        materia.setPlantelId(8L);
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);
        when(materiaRepository.findById("MAT-CAMPUS")).thenReturn(Optional.of(materia));

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.updateEstado("MAT-CAMPUS", false, director));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("otro plantel"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: DOCENTE intenta desactivar cualquier materia.
    // Resultado esperado: HTTP 403 sin cambiar estado.
    @Test
    void updateEstado_shouldRejectUserWithoutCatalogPermission() {
        User docente = usuario(Roles.DOCENTE, 7L);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.updateEstado("MAT-1", false, docente));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("administrar materias"));
        verify(materiaRepository, never()).findById(any());
    }

    // Input: materialId inexistente y Director General.
    // Resultado esperado: EntityNotFoundException rastreable.
    @Test
    void updateEstado_shouldRejectUnknownMateria() {
        User director = usuario(Roles.DIRECTOR_GENERAL, null);
        when(materiaRepository.findById("MAT-404")).thenReturn(Optional.empty());

        EntityNotFoundException error = assertThrows(
            EntityNotFoundException.class,
            () -> materiaService.updateEstado("MAT-404", false, director));

        assertTrue(error.getMessage().contains("MAT-404"));
    }

    // Input: Director General sin plantel seleccionado.
    // Resultado esperado: devuelve únicamente materias con plantelId=null.
    @Test
    void findMateriasVisibles_shouldReturnCommonCoreForDirectorGeneral() {
        Materia base = new Materia();
        base.setId("MAT-BASE");
        base.setNombre("Base");
        base.setPrefijo("BASE");
        base.setPlantelId(null);
        base.setActivo(true);
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        when(materiaRepository.findByPlantelIdIsNull()).thenReturn(List.of(base));

        List<MateriaDTO> result =
            materiaService.findMateriasVisibles(null, director);

        assertEquals(1, result.size());
        assertEquals("MAT-BASE", result.get(0).getId());
        assertEquals(null, result.get(0).getPlantelId());
    }

    // Input: Director General selecciona plantel=7.
    // Resultado esperado: consulta el fallback combinado del plantel.
    @Test
    void findMateriasVisibles_shouldUsePlantelFallbackForDirectorGeneral() {
        Materia base = new Materia();
        base.setId("MAT-BASE");
        base.setNombre("Base");
        base.setPrefijo("BASE");
        base.setPlantelId(null);
        base.setActivo(true);
        User director = usuario(Roles.DIRECTOR_GENERAL, null);

        when(materiaRepository.findByPlantelIdIsNull()).thenReturn(List.of(base));
        when(materiaRepository.findByPlantelId(7L)).thenReturn(List.of());

        List<MateriaDTO> result =
            materiaService.findMateriasVisibles(7L, director);

        assertEquals(1, result.size());
        assertEquals("MAT-BASE", result.get(0).getId());
    }

    // Input: Coordinador sin plantel asignado consulta materias.
    // Resultado esperado: HTTP 403.
    @Test
    void findMateriasVisibles_shouldRejectCampusUserWithoutPlantel() {
        User coordinador = usuario(Roles.COORDINADOR, null);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.findMateriasVisibles(null, coordinador));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("plantel asignado"));
    }

    // Input: Director de Plantel consulta otro plantel.
    // Resultado esperado: HTTP 403.
    @Test
    void findMateriasVisibles_shouldRejectDirectorOfAnotherCampus() {
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.findMateriasVisibles(8L, director));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("permiso"));
    }

    // Input: PATCH con nombre="   " y usuario válido.
    // Resultado esperado: HTTP 400 vía IllegalArgumentException sin guardar.
    @Test
    void updateMateria_shouldRejectBlankName() {
        MateriaDTO dto = MateriaDTO.builder().nombre("   ").build();
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> materiaService.updateMateria("MAT-1", dto, 7L, director));

        assertTrue(error.getMessage().contains("nombre"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: PATCH con nombre de 256 caracteres.
    // Resultado esperado: 400 y sin guardar.
    @Test
    void updateMateria_shouldRejectNameOver255Characters() {
        MateriaDTO dto = MateriaDTO.builder().nombre("N".repeat(256)).build();
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> materiaService.updateMateria("MAT-1", dto, 7L, director));

        assertTrue(error.getMessage().contains("255"));
        verify(materiaRepository, never()).save(any());
    }

    // Input: Director de Plantel intenta eliminar materia tronco común.
    // Resultado esperado: HTTP 403 y no elimina.
    @Test
    void deleteMateria_shouldRejectCommonCoreMateria() {
        Materia materia = new Materia();
        materia.setId("MAT-BASE");
        materia.setPlantelId(null);
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);
        when(materiaRepository.findById("MAT-BASE")).thenReturn(Optional.of(materia));

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> materiaService.deleteMateria("MAT-BASE", director));

        assertEquals(403, error.getStatusCode().value());
        verify(materiaRepository, never()).delete(any());
    }

    // Input: Director de Plantel elimina materia específica propia.
    // Resultado esperado: elimina sin error.
    @Test
    void deleteMateria_shouldAllowOwnCampusMateria() {
        Materia materia = new Materia();
        materia.setId("MAT-CAMPUS");
        materia.setPlantelId(7L);
        User director = usuario(Roles.DIRECTOR_PLANTEL, 7L);
        when(materiaRepository.findById("MAT-CAMPUS")).thenReturn(Optional.of(materia));

        materiaService.deleteMateria("MAT-CAMPUS", director);

        verify(materiaRepository).delete(materia);
    }
}
