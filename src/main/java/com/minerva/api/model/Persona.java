package com.minerva.api.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.minerva.api.User.Roles;

@Getter 
@Setter 
@ToString(exclude = "plantel")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "persona")
public class Persona {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plantel", nullable = false)
    private Plantel plantel;
}