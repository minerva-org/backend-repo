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
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "personas")
public class Persona {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "apellido_paterno")
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false)
    private String apellidoMaterno;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles rol;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plantel", nullable = false)
    private Plantel plantel;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column (name = "calle", length = 50)
    private String calle;

    @Column (name = "colonia", length = 50)
    private String colonia;

    @Column (name = "municipio", length = 50)
    private String municipio;

    @Column (name = "estado", length = 50)
    private String estado;

    @Column (name = "codigo_postal", length = 50)
    private String codigoPostal;

    @Column (name = "numero_telefono", length = 10)
    private String numeroTelefono;

    @Column (name = "password")
    private String password;
}