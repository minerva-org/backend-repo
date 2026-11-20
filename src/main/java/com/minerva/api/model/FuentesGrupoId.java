package com.minerva.api.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FuentesGrupoId implements Serializable {

    @Column(name = "id_documento")
    private String idDocumento;

    @Column(name = "id_grupo")
    private String idGrupo;
}
