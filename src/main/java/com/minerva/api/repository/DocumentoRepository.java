package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Documento;

public interface DocumentoRepository extends JpaRepository<Documento, String> {
    List<Documento> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);

    Optional<Documento> findByChecksumSha256(String checksumSha256);

    Optional<Documento> findByObjectKey(String objectKey);

    boolean existsByChecksumSha256(String checksumSha256);
}