package com.minerva.api.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.minerva.api.dto.DocumentoDTO;

public interface GrupoFuenteService {

    DocumentoDTO uploadFuente(String grupoId, MultipartFile file, String uploadedBy);

    List<DocumentoDTO> findFuentes(String grupoId, String requestedBy);

    void removeFuente(String grupoId, String documentoId, String requestedBy);
}
