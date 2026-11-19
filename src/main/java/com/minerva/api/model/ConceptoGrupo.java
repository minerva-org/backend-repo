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
@Table(name = "concepto_grupo")
public class ConceptoGrupo {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tema_grupo", nullable = false)
    private TemaGrupo temaGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_concepto_origen")
    private Concepto conceptoOrigen;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "conceptoGrupo", fetch = FetchType.LAZY)
    private List<ContenidoGrupo> contenidosGrupo = new ArrayList<>();
}
