CREATE TABLE resumen_quiz (
    id               VARCHAR(36)      NOT NULL,
    id_quiz          VARCHAR(255)     NOT NULL,
    tipo_quiz        VARCHAR(30)              ,
    num_respondieron INTEGER          NOT NULL DEFAULT 0,
    promedio         DOUBLE PRECISION NOT NULL DEFAULT 0,
    num_aprobados    INTEGER          NOT NULL DEFAULT 0,

    CONSTRAINT pk_resumen_quiz PRIMARY KEY (id),
    CONSTRAINT fk_resumen_quiz_quiz FOREIGN KEY (id_quiz) REFERENCES quiz (id) ON DELETE CASCADE,
    CONSTRAINT uq_resumen_quiz_quiz UNIQUE (id_quiz)
);