package com.minerva.api.service;

import java.util.List;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.Roles;
import com.minerva.api.dto.DocumentoDTO;
import com.minerva.api.dto.FileDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Documento;
import com.minerva.api.model.FuentesGrupo;
import com.minerva.api.model.FuentesGrupoId;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.repository.DocumentoRepository;
import com.minerva.api.repository.FuentesGrupoRepository;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrupoFuenteServiceImpl implements GrupoFuenteService {

    private final GrupoRepository grupoRepository;
    private final PersonaRepository personaRepository;
    private final DocumentoRepository documentoRepository;
    private final FuentesGrupoRepository fuentesGrupoRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public DocumentoDTO uploadFuente(String grupoId, MultipartFile file, String uploadedBy) {
        Grupo grupo = buscarGrupoConPermiso(grupoId, uploadedBy);
        Persona persona = buscarPersona(uploadedBy);

        if (persona.getRol() == Roles.ALUMNO) {
            throw accesoDenegado();
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo.");
        }

        String checksum = sha256Hex(content);
        Optional<Documento> documentoExistente =
                documentoRepository.findFirstByChecksumSha256OrderByCreatedAtDesc(checksum);

        Documento documento;
        if (documentoExistente.isPresent()) {
            documento = documentoExistente.get();
        } else {
            FileDTO fileDto = fileStorageService.upload(file, uploadedBy);
            documento = documentoRepository.findById(fileDto.fileId().toString())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No se encontró el documento recién almacenado."));
        }

        FuentesGrupoId id = new FuentesGrupoId(documento.getId(), grupo.getId());
        if (!fuentesGrupoRepository.existsById(id)) {
            FuentesGrupo asociacion = new FuentesGrupo();
            asociacion.setId(id);
            asociacion.setDocumento(documento);
            asociacion.setGrupo(grupo);
            fuentesGrupoRepository.save(asociacion);
        }

        return Mapper.toDTO(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoDTO> findFuentes(String grupoId, String requestedBy) {
        buscarGrupoConPermiso(grupoId, requestedBy);
        return fuentesGrupoRepository.findByGrupoIdOrderByDocumentoCreatedAtDesc(grupoId)
                .stream()
                .map(fuente -> Mapper.toDTO(fuente.getDocumento()))
                .toList();
    }

    @Override
    @Transactional
    public void removeFuente(String grupoId, String documentoId, String requestedBy) {
        Grupo grupo = buscarGrupoConPermiso(grupoId, requestedBy);
        FuentesGrupoId id = new FuentesGrupoId(documentoId, grupo.getId());
        if (!fuentesGrupoRepository.existsById(id)) {
            throw new EntityNotFoundException("La fuente no está asociada al grupo.");
        }
        fuentesGrupoRepository.deleteById(id);
    }

    private Grupo buscarGrupoConPermiso(String grupoId, String personaId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Grupo no encontrado con el id: " + grupoId));
        Persona persona = buscarPersona(personaId);

        Roles rol = persona.getRol();
        if (rol == Roles.ADMIN || rol == Roles.DEV || rol == Roles.DIRECTOR_GENERAL) {
            return grupo;
        }

        if (rol == Roles.DOCENTE) {
            if (grupo.getDocente() == null
                    || !grupo.getDocente().getId().equals(persona.getId())) {
                throw accesoDenegado();
            }
            return grupo;
        }

        if (rol == Roles.COORDINADOR || rol == Roles.DIRECTOR_PLANTEL) {
            Long plantelUsuario = persona.getPlantel() != null
                    ? persona.getPlantel().getId()
                    : null;
            if (plantelUsuario == null
                    || grupo.getPlantel() == null
                    || !plantelUsuario.equals(grupo.getPlantel().getId())) {
                throw accesoDenegado();
            }
            return grupo;
        }

        throw accesoDenegado();
    }

    private Persona buscarPersona(String personaId) {
        return personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Persona no encontrada con el id: " + personaId));
    }

    private ResponseStatusException accesoDenegado() {
        return new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para administrar las fuentes de este grupo.");
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
