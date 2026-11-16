package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.PeriodoDTO;

public interface PeriodoService {
    List<PeriodoDTO> findAll();
    
    PeriodoDTO nuevoPeriodo();
    
}
