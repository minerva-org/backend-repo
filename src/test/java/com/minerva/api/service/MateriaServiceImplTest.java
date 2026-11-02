package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class MateriaServiceImplTest {

    @Mock
    private MateriaRepository materiaRepository;

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
        when(materiaRepository.existsByNombreIgnoreCase("Matemáticas")).thenReturn(false);
        when(unidadRepository.findByMateriaId("MAT-001")).thenReturn(java.util.Collections.emptyList());
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocation -> {
            Materia materia = invocation.getArgument(0);
            materia.setId("MAT-001");
            return materia;
        });

        MateriaDTO result = materiaService.saveMateria(dto);

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
            .unidades(java.util.List.of(
                UnidadDTO.builder()
                    .id("UNI-1")
                    .nombre("Revolución")
                    .temas(java.util.List.of(
                        TemaDTO.builder()
                            .id("TEM-1")
                            .nombre("Causas")
                            .conceptos(java.util.List.of(
                                ConceptoDTO.builder().id("CON-1").nombre("Industrialización").build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .build();

        when(materiaRepository.existsById("MAT-002")).thenReturn(false);
        when(materiaRepository.existsByNombreIgnoreCase("Historia")).thenReturn(false);
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

        when(unidadRepository.findByMateriaId("MAT-002")).thenReturn(java.util.List.of(unidadPersistida));
        when(temaRepository.findByUnidadId("UNI-1")).thenReturn(java.util.List.of(temaPersistido));
        when(conceptoRepository.findByTemaId("TEM-1")).thenReturn(java.util.List.of(conceptoPersistido));

        MateriaDTO result = materiaService.saveMateria(dto);

        assertNotNull(result);
        assertEquals("MAT-002", result.getId());
        assertEquals(1, result.getUnidades().size());
        assertEquals("UNI-1", result.getUnidades().get(0).getId());
        assertEquals(1, result.getUnidades().get(0).getTemas().size());
        assertEquals("CON-1", result.getUnidades().get(0).getTemas().get(0).getConceptos().get(0).getId());
    }
}
