package com.minerva.api.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fuentes_grupo")
public class FuentesGrupo {

    @EmbeddedId
    private FuentesGrupoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idDocumento")
    @JoinColumn(name = "id_documento", nullable = false)
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idGrupo")
    @JoinColumn(name = "id_grupo", nullable = false)
    private Grupo grupo;
}
