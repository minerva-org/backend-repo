package com.minerva.api.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.PeriodoDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Periodo;
import com.minerva.api.repository.PeriodoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PeriodoServiceImpl implements PeriodoService {

    private final PeriodoRepository periodoRepository;

    @Override
    public List<PeriodoDTO> findAll() {
        return periodoRepository.findAll()
                .stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @Override
    public PeriodoDTO nuevoPeriodo() {
        Periodo periodo = periodoRepository.findFirstByOrderByFechaCreacionDesc();

        String curso;
        String descripcion;
        if (periodo.getDescripcion().equals("ENE-JUN")) {
            descripcion = ("AGO-DIC");
            curso = (periodo.getCurso());
        } else {
            descripcion = ("ENE-JUN");
            int anio = Integer.parseInt(periodo.getCurso()) + 1;
            curso = (String.valueOf(anio));
        }

        Periodo nuevoPeriodo = new Periodo();
        nuevoPeriodo.setDescripcion(descripcion);
        nuevoPeriodo.setCurso(curso);
        nuevoPeriodo.setFechaCreacion(Instant.now());

        periodoRepository.save(nuevoPeriodo);
        return Mapper.toDTO(nuevoPeriodo);
    }

}
