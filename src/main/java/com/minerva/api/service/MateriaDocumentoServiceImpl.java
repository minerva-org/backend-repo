package com.minerva.api.service;

import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.minerva.api.dto.DocumentoDTO;
import com.minerva.api.dto.DocumentoMateriaUploadDTO;
import com.minerva.api.dto.FileDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.User.Roles;
import com.minerva.api.model.Documento;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Persona;
import com.minerva.api.repository.DocumentoRepository;
import com.minerva.api.repository.MateriaRepository;
import com.minerva.api.repository.PersonaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MateriaDocumentoServiceImpl implements MateriaDocumentoService {

    private final MateriaRepository materiaRepository;
    private final DocumentoRepository documentoRepository;
    private final FileStorageService fileStorageService;
    private final PersonaRepository personaRepository;

    private void validarAccesoPlan(Materia materia, String uploadedBy) {
        Persona persona = personaRepository.findById(uploadedBy)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Persona no encontrada con el id: " + uploadedBy));

        Roles rol = persona.getRol();
        if (rol == Roles.ADMIN || rol == Roles.DEV || rol == Roles.DIRECTOR_GENERAL) {
            return;
        }

        if (rol == Roles.DIRECTOR_PLANTEL || rol == Roles.COORDINADOR) {
            Long plantelUsuario = persona.getPlantel() != null
                    ? persona.getPlantel().getId()
                    : null;
            if (plantelUsuario == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes permiso para modificar el plan de esta materia.");
            }
            if (materia.getPlantelId() != null
                    && !materia.getPlantelId().equals(plantelUsuario)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes permiso para modificar el plan de esta materia.");
            }
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para modificar planes de estudio.");
    }

    @Override
    @Transactional
    public DocumentoMateriaUploadDTO uploadPlan(MultipartFile file, String materiaId, String uploadedBy) {
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Materia no encontrada con el id: " + materiaId));
        validarAccesoPlan(materia, uploadedBy);

        byte[] content;
        try {
            content = file.getBytes();
        } catch (java.io.IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo.");
        }

        String checksum = sha256Hex(content);
        Optional<Documento> documentoExistente = documentoRepository.findByChecksumSha256(checksum);

        Documento documento;
        boolean reutilizado;
        if (documentoExistente.isPresent()) {
            documento = documentoExistente.get();
            reutilizado = true;
        } else {
            FileDTO fileDto = fileStorageService.upload(file, uploadedBy);
            documento = documentoRepository.findById(fileDto.fileId().toString())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No se encontró el documento recién almacenado."));
            reutilizado = false;
        }

        materia.setDocumentoMateria(documento);
        materiaRepository.save(materia);
        return new DocumentoMateriaUploadDTO(Mapper.toDTO(documento), reutilizado);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoDTO getPlanMateria(String materiaId) {
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Materia no encontrada con el id: " + materiaId));
        if (materia.getDocumentoMateria() == null) {
            throw new EntityNotFoundException(
                    "La materia no tiene documento de plan de estudio asociado.");
        }
        return Mapper.toDTO(materia.getDocumentoMateria());
    }

    @Override
    @Transactional
    public void removePlanMateria(String materiaId, String removedBy) {
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Materia no encontrada con el id: " + materiaId));
        materia.setDocumentoMateria(null);
        materiaRepository.save(materia);
    }

    private String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }
}
