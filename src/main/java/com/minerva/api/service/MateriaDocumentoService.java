package com.minerva.api.service;

import org.springframework.web.multipart.MultipartFile;

import com.minerva.api.dto.DocumentoDTO;
import com.minerva.api.dto.DocumentoMateriaUploadDTO;

public interface MateriaDocumentoService {

    DocumentoMateriaUploadDTO uploadPlan(MultipartFile file, String materiaId, String uploadedBy);

    DocumentoDTO getPlanMateria(String materiaId);

    void removePlanMateria(String materiaId, String removedBy);
}
