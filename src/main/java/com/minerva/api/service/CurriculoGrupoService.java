package com.minerva.api.service;

import com.minerva.api.dto.CurriculoGrupoDTO;
import com.minerva.api.dto.UnidadGrupoDTO;

public interface CurriculoGrupoService {

    CurriculoGrupoDTO findByGrupoId(String grupoId);

    CurriculoGrupoDTO replaceCurriculo(String grupoId, CurriculoGrupoDTO curriculo);

    UnidadGrupoDTO findUnidadById(String grupoId, String unidadGrupoId);
}
