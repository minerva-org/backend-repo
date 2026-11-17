# Minerva API — Backend

API REST del proyecto Minerva, construida con Spring Boot.

## Stack

- **Java 26 (LTS)** 
- **Spring Boot 4.x** (Spring Framework 7)
- **Maven** (via wrapper, no requiere instalación global)
- **PostgreSQL**
- **Docker Compose** — para levantar Postgres en local sin instalarlo a mano

### Dependencias principales

- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- Lombok
- Spring Boot DevTools
- Validation
- Flyway (migraciones de base de datos)
- Actuator (health checks / métricas)
- Testcontainers (tests de integración con Postgres real)

## Requisitos previos

- **JDK 26** 
- **Docker** y **Docker Compose** — para la base de datos local.
- No necesitas Maven instalado globalmente, el repo trae `mvnw` / `mvnw.cmd`.

## Setup inicial

```bash
git clone <url-del-repo>
cd backend-repo

# Levanta Postgres y MinIO en Docker
docker compose up -d postgres minio

# Crea el bucket privado de MinIO (requiere tener instalado MinIO Client, `mc`)
set -a; . ./.env; set +a
mc alias set minerva http://localhost:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"
mc mb --ignore-existing "minerva/${S3_BUCKET:-minerva-documents-dev}"

# Corre la app (descarga dependencias la primera vez)
./mvnw spring-boot:run
```

La API debería quedar corriendo en `http://localhost:8080` (ajustar si el puerto está configurado distinto).
El bucket se crea privado; si no defines `S3_BUCKET`, se usa `minerva-documents-dev`.

## Comandos útiles

| Comando | Qué hace |
|---|---|
| `./mvnw spring-boot:run` | Levanta la app en modo desarrollo |
| `./mvnw test` | Corre los tests (usa Testcontainers, requiere Docker corriendo) |
| `./mvnw clean install` | Compila y empaqueta el proyecto |
| `docker compose up -d` | Levanta Postgres (y otros servicios definidos en `compose.yaml`) en segundo plano |
| `docker compose down` | Detiene los contenedores |

## Configuración

La configuración vive en `src/main/resources/application.properties` (o `.yml` si se migra más adelante). Variables sensibles (credenciales de base de datos, secrets de JWT, etc.) no deben commitearse — usar variables de entorno o un `application-local.properties` ignorado por git.

Los puertos publicados por Docker Compose se pueden configurar en el `.env` local:

| Variable | Valor predeterminado | Servicio |
|---|---:|---|
| `POSTGRES_PORT` | 5433 | PostgreSQL |
| `API_PORT` | 8080 | API |
| `CADDY_PORT` | 8090 | Caddy |

Los puertos se publican solo en `127.0.0.1`; los puertos internos de los contenedores no cambian. Si cambias `API_PORT` y usas el servidor de desarrollo web, configura `VITE_API_URL` en el entorno de Vite para apuntar a `http://localhost:<API_PORT>`. Si cambias `CADDY_PORT` y usas Tailscale Funnel, actualiza el puerto de destino de Funnel.

<!-- TODO: documentar las variables de entorno reales una vez definidas -->

## Laboratorio de agente (solo DEV)

El frontend usa estos endpoints con su JWT habitual:

| Metodo | Ruta | Funcion |
|---|---|---|
| GET | `/api/ai/laboratory/documents/prompts` | Cargar las tres capas iniciales desde AI-Service |
| POST | `/api/ai/laboratory/documents/extract` | Reenviar PDF y configuracion de una prueba |

Ambos requieren autoridad `DEV` o `ROLE_DEV`. El POST recibe multipart con
`file` y una parte `request` de tipo `application/json` que contiene
`sourceType` (`STUDY_PLAN` o `SOURCE_MATERIAL`) y `prompts`
(`role`, `instructions`, `outputContract`).

Se utilizan `AI_SERVICE_BASE_URL` y `AI_SERVICE_API_KEY`, igual que para el
health. La clave interna se agrega en el backend y nunca se envia al navegador.
No se necesita configurar el modelo en la API: lo elige AI-Service.
El cliente del laboratorio tiene 10 s de conexion y 240 s de espera de respuesta;
el tiempo de Ollama debe mantenerse por debajo de ese presupuesto.

La API conserva HTTP 422 y los diagnosticos del modelo, asi como errores de
entrada/disponibilidad del servicio. Errores internos de autenticacion de
AI-Service o respuestas incompatibles se presentan como HTTP 502.
Las respuestas reenviadas usan `Cache-Control: no-store`.
Los limites multipart existentes son 10 MiB por archivo y 11 MiB por solicitud.
No se guarda ningun documento ni arbol y no se requieren migraciones.

## Migraciones de base de datos

Las migraciones se manejan con Flyway, ubicadas en `src/main/resources/db/migration/`. **No uses `ddl-auto: update`** en ningún ambiente — todo cambio de esquema debe ir en una migración versionada.

## Usuarios por defecto

Al levantar el backend con la carga inicial de datos, se crean usuarios de prueba para desarrollo local. Los más útiles son:

| Usuario | Contraseña | Rol / Uso |
|---|---|---|
| `bootstrap` | `Admin123!` | Usuario inicial del sistema para pruebas rápidas y bootstrap |
| `director_general` | `Admin123!` | Director general |
| `director_plantel` | `Admin123!` | Director de plantel |
| `docente` | `Admin123!` | Docente |
| `coordinador` | `Admin123!` | Coordinador |
| `alumno` | `Admin123!` | Alumno |

Puedes probar el login con una petición como esta:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"bootstrap","password":"Admin123!"}'
```

> Si cambias la semilla de usuarios en el backend o la configuración de seguridad, actualiza esta sección para evitar confusión entre el equipo.

## Troubleshooting común

**La app no conecta a la base de datos**
Verifica que `docker compose up -d` esté corriendo (`docker ps` para confirmar), y que el `application.properties` apunte al host/puerto correcto.

**Tests fallan por Docker**
Testcontainers necesita Docker corriendo localmente para los tests de integración. Si no tienes Docker activo, esos tests van a fallar.
