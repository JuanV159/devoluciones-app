# DevolucionesApp

MVP de plataforma de devoluciones (prueba técnica Full Stack — Java 21 / Angular 17).

## Requisitos

- JDK 21
- Maven 3.9+
- Node 18.19+ / 20.11+ (o compatible)
- Angular CLI 17 (`npx @angular/cli@17`)
- Docker + Docker Compose
- PostgreSQL 16 (vía Docker)

## Cómo levantar (desarrollo local)

```bash
cp .env.example .env
docker compose up -d db
cd backend && ./mvnw spring-boot:run
cd frontend && npm start
```

- API: http://localhost:8080/actuator/health  
- UI: http://localhost:4200  

> En Windows PowerShell usa `.\mvnw.cmd` en lugar de `./mvnw`.

## Estructura

```
devoluciones-app/
├── backend/     Spring Boot 3.3 · Java 21 · Flyway · PostgreSQL
├── frontend/    Angular 17 (standalone)
├── docs/        CSV de ejemplo y colección .http
├── docker-compose.yml
└── .env.example
```

## Estado del proyecto

- Esqueleto monorepo + Docker
- Dominio: máquina de estados R1–R7 con tests
- Persistencia: Flyway V1 (schema) + V2 (seed) y entidades JPA
- API REST: solicitudes + transiciones (Parte 1)
- Carga masiva CSV (Parte 2)

Próximo: JWT + frontend.

## Usuarios seed

| Usuario       | Rol         | Password        |
|---------------|-------------|-----------------|
| analista1     | ANALISTA    | `Password123!`  |
| supervisor1   | SUPERVISOR  | `Password123!`  |

Hay **10 solicitudes** de ejemplo en distintos estados, cada una con histórico coherente.

> Si cambiaste `V1__schema.sql` tras haber levantado la DB antes, recrea el volumen:  
> `docker compose down -v && docker compose up -d db`

## Decisiones de diseño

### Máquina de estados (R1–R7)

- Vive en `cl.nxtara.devoluciones.domain.MaquinaEstados`: mapa `Estado × Accion → Estado` + validaciones.
- Las transiciones son **acciones** (`ENVIAR`, `APROBAR`, …), no un `PUT` del campo `estado`.
- **R4 (reabrir 1 vez):** contador `reaperturas` en la solicitud (consulta O(1) y evita condiciones de carrera). El `EventoSolicitud` queda como evidencia histórica.
- **R2:** solo `APROBAR`, `RECHAZAR` y `PAGAR` exigen `SUPERVISOR`; `ENVIAR`, `ANULAR` y `REABRIR` bastan con `ANALISTA`.
- **R6:** el dominio devuelve `TransicionResultado`; la capa de aplicación debe persistir solicitud + evento en la misma `@Transactional`.
- **R7:** si el aprobador es el mismo usuario que creó → conflicto de negocio (409), no 403 (el rol sí es SUPERVISOR).

### Persistencia (Flyway)

- `V1__schema.sql`: `usuario`, `solicitud`, `evento_solicitud`, `folio_secuencia`.
- `V2__seed.sql`: 2 usuarios BCrypt + 10 solicitudes con eventos coherentes.
- `referencia_banco` UNIQUE (idempotencia de carga masiva).
- Entidades JPA en `infrastructure.persistence` (no se exponen por la API).

### API REST (identidad temporal)

- Endpoints bajo `/api/v1/solicitudes` con acciones (`/enviar`, `/aprobar`, …), no `PUT` de `estado`.
- Hasta la Parte 4 (JWT), la identidad viaja en headers `X-Usuario` y `X-Rol`.
- Errores unificados: `timestamp`, `status`, `error`, `detalle`, `path`.
- Colección reproducible: `docs/ciclo-vida.http`.

### Carga masiva CSV

- `POST /api/v1/cargas` (multipart) y `GET /api/v1/cargas/{id}`.
- **Batch por chunks** (default 200): `save` + `flush/clear` por lote. Un INSERT+commit por fila sería órdenes de magnitud más lento por el overhead de transacciones.
- **Tolerancia:** filas inválidas se registran en `carga_error` y el resto continúa.
- **Idempotencia:** `UNIQUE(referencia_banco)`. Re-subir el mismo archivo no duplica; las ya existentes cuentan como `filasOmitidas`.
- **Transaccionalidad:** commit por chunk (no todo-o-nada). Si el proceso muere en la fila 700, lo ya confirmado queda; al reintentar, el unique evita duplicados.
- Solicitudes nacen en `EN_REVISION` con `origen=CARGA_MASIVA` y evento inicial.
- **Bonus asíncrono (diseño):** con 50k filas se respondería `202 Accepted` + job id y el procesamiento correría en un `@Async`/cola; el `GET /cargas/{id}` ya sirve como endpoint de estado (`PROCESANDO` → `COMPLETADA`).

### Pendiente de documentar

JWT (dónde guardar el token), RxJS vs signals.
