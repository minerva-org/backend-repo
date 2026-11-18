package com.minerva.api.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.DocumentoDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Documento;
import com.minerva.api.repository.DocumentoRepository;
import com.minerva.api.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    private final PersonaRepository personaRepository;
    private final DocumentoRepository documentoRepository;

    public DocumentoDTO saveDocumento(DocumentoDTO documento){
        
        if(documento.objectKey() == null || documento.objectKey().isBlank()){
            throw new IllegalArgumentException("El object key es obligatorio");
        }
        
        if(documento.originalFilename() == null || documento.originalFilename().isBlank()){
            throw new IllegalArgumentException("El nombre original es obligatorio");
        }
        
        if(documento.contentType() == null || documento.contentType().isBlank()){
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        
        if(documento.sizeBytes() <= 0){
            throw new IllegalArgumentException("El peso del archivo es obligatorio");
        }
        
        if(documento.uploadedBy() == null || documento.uploadedBy().isBlank()){
            throw new IllegalArgumentException("El registro de quien subió el archivo es obligatorio");
        }

        if(!personaRepository.existsById(documento.uploadedBy())){
            throw new IllegalArgumentException("No se encontró un usuario con el id: " + documento.uploadedBy());
        }

        Documento nvoDocumento = new Documento();
        nvoDocumento.setObjectKey(documento.objectKey());
        nvoDocumento.setOriginalFilename(documento.originalFilename());
        nvoDocumento.setContentType(documento.contentType());
        nvoDocumento.setSizeBytes(documento.sizeBytes());
        nvoDocumento.setChecksumSha256(documento.checksumSha256());
        nvoDocumento.setUploadedBy(documento.uploadedBy().trim());
        nvoDocumento.setCreatedAt(Instant.now());
        documentoRepository.save(nvoDocumento);
        
        return Mapper.toDTO(nvoDocumento);
    }
}
