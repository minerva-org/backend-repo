package com.minerva.api.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "resumen_quiz")
public class ResumenQuiz {

    @Id
    @Column(name = "id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_quiz", nullable = false, unique = true)
    private Quiz quiz;

    @Column(name = "tipo_quiz")
    private String tipoQuiz;

    @Column(name = "num_respondieron", nullable = false)
    private int numRespondieron;

    @Column(name = "promedio", nullable = false)
    private double promedio;

    @Column(name = "num_aprobados", nullable = false)
    private int numAprobados;

    @PrePersist
    private void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}