package com.minerva.api.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"docente", "alumnos"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "grupo")
public class Grupo {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private String id;

    @Column(name = "clave_grupo", nullable = false, unique = true)
    private String claveGrupo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "semestre", nullable = false)
    private String semestre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_docente", nullable = false)
    private Persona docente;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "grupos_alumnos",
        joinColumns = @JoinColumn(name = "id_grupo"),
        inverseJoinColumns = @JoinColumn(name = "id_alumno")
    )
    private Set<Persona> alumnos = new HashSet<>();

    public void addAlumno(Persona alumno) {
        alumnos.add(alumno);
    }

    public void removeAlumno(Persona alumno) {
        alumnos.remove(alumno);
    }
}