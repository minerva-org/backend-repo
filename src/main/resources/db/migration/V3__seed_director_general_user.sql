INSERT INTO persona (id, nombre, apellido, email, rol, id_plantel)
VALUES ('DIRECTOR-GENERAL-DEFAULT', 'Director', 'General', 'director.general@minerva.local', 'DIRECTOR_GENERAL', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios (id, username, password, reestablecimiento, id_persona)
VALUES (
    2,
    'director_general',
    '$2a$10$aPgMZCFTKTN0OW07V3LDkubtzo1yzWFk7apCQtHANC91ZJX3RSUEW',
    FALSE,
    'DIRECTOR-GENERAL-DEFAULT'
)
ON CONFLICT (username) DO NOTHING;
