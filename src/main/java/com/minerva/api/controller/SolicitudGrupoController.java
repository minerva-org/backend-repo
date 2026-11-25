package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.minerva.api.User.User;
import com.minerva.api.dto.SolicitudesGruposRequestDTO;
import com.minerva.api.dto.SolicitudesGruposResponseDTO;
import com.minerva.api.service.SolicitudesGrupoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/solicitud")
public class SolicitudGrupoController {

    private final SolicitudesGrupoService solicitudService;

    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudesGruposResponseDTO>> getSolicitudesMisGrupos(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String id = user.getPersona().getId();
        return ResponseEntity.ok(solicitudService.findByDocenteId(id));
    }

    @PostMapping
    public ResponseEntity<SolicitudesGruposResponseDTO> crear(
            @RequestBody SolicitudesGruposRequestDTO request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.saveSolicitud(request.grupoId(), user.getPersona().getId()));
    }

    @PostMapping("/{solicitudId}/aceptar")
    public ResponseEntity<Void> aceptar(@PathVariable String solicitudId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String id = user.getPersona().getId();
        solicitudService.aceptar(solicitudId, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{solicitudId}/rechazar")
    public ResponseEntity<Void> rechazar(@PathVariable String solicitudId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String id = user.getPersona().getId();
        solicitudService.rechazar(solicitudId, id);
        return ResponseEntity.noContent().build();
    }

}