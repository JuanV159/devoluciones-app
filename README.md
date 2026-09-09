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

Esqueleto inicial del monorepo. Próximos commits: dominio (máquina de estados), API REST, carga CSV y frontend.

## Usuarios seed (próximamente)

| Usuario       | Rol         |
|---------------|-------------|
| analista1     | ANALISTA    |
| supervisor1   | SUPERVISOR  |

## Decisiones de diseño

### Máquina de estados (R1–R7)

- Vive en `cl.nxtara.devoluciones.domain.MaquinaEstados`: mapa `Estado × Accion → Estado` + validaciones.
- Las transiciones son **acciones** (`ENVIAR`, `APROBAR`, …), no un `PUT` del campo `estado`.
- **R4 (reabrir 1 vez):** contador `reaperturas` en la solicitud (consulta O(1) y evita condiciones de carrera). El `EventoSolicitud` queda como evidencia histórica.
- **R2:** solo `APROBAR`, `RECHAZAR` y `PAGAR` exigen `SUPERVISOR`; `ENVIAR`, `ANULAR` y `REABRIR` bastan con `ANALISTA`.
- **R6:** el dominio devuelve `TransicionResultado`; la capa de aplicación debe persistir solicitud + evento en la misma `@Transactional`.
- **R7:** si el aprobador es el mismo usuario que creó → conflicto de negocio (409), no 403 (el rol sí es SUPERVISOR).

### Pendiente de documentar

Transaccionalidad de la carga CSV, JWT (dónde guardar el token), RxJS vs signals.
