INSERT INTO universidad (id, detalles, activo)
VALUES (1, 'Universidad Minerva', TRUE);

INSERT INTO planteles (id, detalles, calle, colonia, municipio, id_universidad, activo)
VALUES (1, 'Plantel Central', 'Sin direccion inicial', 'Sin colonia inicial', 'Sin municipio inicial', 1, TRUE);

INSERT INTO personas (id, nombre, apellido_materno, email, rol, id_plantel)
VALUES ('BOOTSTRAP-DEV', 'Bootstrap', 'Admin', 'bootstrap@minerva.local', 'DEV', 1);

INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES (
    'bootstrap',
    '$2a$10$Sea1h7vQ6nDzzuhQFyWLD.p1/Nxt4JVvJ226g9eMjRmZatTGMUQ/C',
    FALSE,
    'BOOTSTRAP-DEV'
);