package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Documento;

public interface DocumentoRepository extends JpaRepository<Documento, String> {
    List<Documento> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);
}