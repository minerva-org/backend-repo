package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tema_grupo")
public class TemaGrupo {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "clave")
    private String clave;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad_grupo", nullable = false)
    private UnidadGrupo unidadGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema_origen")
    private Tema temaOrigen;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "temaGrupo", fetch = FetchType.LAZY)
    private List<ConceptoGrupo> conceptosGrupo = new ArrayList<>();
}
