# DevolucionesApp

MVP de plataforma de devoluciones (prueba técnica Full Stack — Java 21 / Angular 17 / PostgreSQL 16).

## Requisitos

- JDK 21
- Maven 3.9+
- Node 18.19+ / 20.11+ (o compatible)
- Angular CLI 17 (`npx @angular/cli@17`)
- Docker + Docker Compose
- PostgreSQL 16 (vía Docker; el compose publica el puerto **5433** en el host)

## Cómo levantar (desarrollo local)

```bash
cp .env.example .env
docker compose up -d db
```

### Backend

Spring **no carga `.env` solo**. Exporta las variables y luego arranca:

**Linux / macOS**

```bash
set -a && source .env && set +a
cd backend && ./mvnw spring-boot:run
```

**Windows PowerShell**

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
  $k, $v = $_ -split '=', 2
  Set-Item -Path "Env:$($k.Trim())" -Value $v.Trim()
}
cd backend
.\mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd frontend && npm start
```

- API health: http://localhost:8080/actuator/health  
- UI: http://localhost:4200 (proxy `/api` → `:8080`)

### Notas de entorno

- El contenedor Postgres usa **`localhost:5433`** (ver `.env.example`). Así se evita choque con un PostgreSQL local en el puerto 5432.
- Si el schema quedó desfasado: `docker compose down -v && docker compose up -d db`.

## Tests

```bash
cd backend && ./mvnw test          # Windows: .\mvnw.cmd test
cd frontend && npm test            # smoke del AppComponent
```

## Estructura

```
devoluciones-app/
├── backend/     Spring Boot 3.3 · Java 21 · Flyway · JWT
├── frontend/    Angular 17 standalone (lazy: auth, solicitudes, cargas)
├── docs/        CSV de ejemplo + ciclo-vida.http
├── docker-compose.yml
└── .env.example
```

## Estado del proyecto

| Parte | Contenido |
|-------|-----------|
| 1 | API REST solicitudes + máquina de estados R1–R7 |
| 2 | Carga masiva CSV (batch, tolerancia, idempotencia) |
| 3 | Frontend: login, bandeja, detalle, form, carga CSV |
| 4 | JWT + RBAC |
| 5 | Reporte de conciliación SQL (`GET /api/v1/reportes/conciliacion`) |

## Usuarios seed

| Usuario       | Rol         | Password        |
|---------------|-------------|-----------------|
| analista1     | ANALISTA    | `Password123!`  |
| supervisor1   | SUPERVISOR  | `Password123!`  |

Hay **10 solicitudes** de ejemplo en distintos estados, cada una con histórico coherente.

CSV de demo: `docs/pagos_banco_ejemplo.csv` (~1000 filas → ~950 OK / ~50 rechazadas).

## Colección HTTP

`docs/ciclo-vida.http` (REST Client / IntelliJ):

1. Login analista / supervisor  
2. Crear → enviar → aprobar → pagar  
3. Historial  
4. Transición inválida (**409**, pagar de nuevo)  
5. Analista intenta aprobar (**403**)  
6. Listado filtrado  

La carga CSV se prueba desde la UI (`/cargas`) o con multipart hacia `POST /api/v1/cargas`.

## Guion corto de demo

1. Login `analista1` → bandeja y filtros.  
2. Crear solicitud → ENVIAR.  
3. Login `supervisor1` → APROBAR → PAGAR.  
4. Con analista: intentar acción de supervisor (403 en UI).  
5. Carga CSV de ejemplo → resumen 950/50; re-subir → omitidas ↑.  

## Decisiones de diseño

### Máquina de estados (R1–R7)

- Vive en `cl.nxtara.devoluciones.domain.MaquinaEstados`: mapa `Estado × Accion → Estado` + validaciones.
- Las transiciones son **acciones** (`ENVIAR`, `APROBAR`, …), no un `PUT` del campo `estado`.
- **R4 (reabrir 1 vez):** contador `reaperturas` en la solicitud (consulta O(1)). El `EventoSolicitud` queda como evidencia.
- **R2:** solo `APROBAR`, `RECHAZAR` y `PAGAR` exigen `SUPERVISOR`.
- **R6:** dominio devuelve `TransicionResultado`; la aplicación persiste solicitud + evento en la misma `@Transactional`.
- **R7:** creador = aprobador → **409** (regla de negocio), no 403.

### Persistencia (Flyway)

- `V1` schema + `V2` seed + `V3` cargas.
- `referencia_banco` UNIQUE (idempotencia de carga).
- Entidades JPA en `infrastructure.persistence` (no se exponen por la API).

### API REST

- `/api/v1/solicitudes` con acciones; errores unificados (`timestamp`, `status`, `error`, `detalle`, `path`).
- Autenticación: `POST /api/v1/auth/login` → JWT Bearer.

### Seguridad JWT

- Spring Security **stateless** + filtro JWT + BCrypt.
- **401** sin/ inválido token; **403** rol insuficiente (R2); **409** regla de negocio (R7 / transición inválida según caso).
- Token en el cliente: **`sessionStorage`**. El JWT es de corta vida y la demo suele hacerse en un equipo compartido; al cerrar la pestaña el token desaparece (frente a `localStorage`). Viaja solo en `Authorization: Bearer`.

### Frontend

- Standalone + lazy (`auth`, `solicitudes`, `cargas`).
- Interceptor JWT + `authGuard`; proxy en `:4200` → `:8080`.
- Estado: **RxJS** en services (HTTP) + **signals** en componentes (loading/error/sesión). Coherente y fácil de justificar en demo: streams para I/O, signals para UI local.
- Validaciones de RUT/monto en el form **espejan** al backend; la autoridad sigue siendo la API.

### Carga masiva CSV

- `POST /api/v1/cargas` (multipart) y `GET /api/v1/cargas/{id}`.
- **Batch por chunks** (default 200): un commit por fila sería mucho más lento por overhead de transacciones.
- **Tolerancia:** filas inválidas → `carga_error`; el resto continúa.
- **Idempotencia:** `UNIQUE(referencia_banco)` → re-subir no duplica (`filasOmitidas`).
- **Transaccionalidad:** commit por chunk (no todo-o-nada). Si el proceso muere a mitad, lo confirmado queda; el unique evita duplicados al reintentar.
- Solicitudes de carga nacen en `EN_REVISION` con `origen=CARGA_MASIVA`.
- **Escenario 50k (diseño):** `202 Accepted` + job async; el `GET` ya modela `PROCESANDO` → `COMPLETADA`.

## Qué faltó y qué haría distinto en producción

### Qué faltó en este MVP

- Extensión de perfil (no llegó enunciado adicional).
- Refresh token / logout server-side; rate limiting en login.
- Observabilidad (métricas de carga, tracing) y CI formal en el repo.

### Qué haría distinto en producción

- Secretos y DB solo por el orquestador (no `.env` en disco del desarrollador sin vault).
- Carga 50k+ siempre asíncrona + cola; DLQ para chunks fallidos.
- Índices acordes a filtros reales (`estado`, `fecha_creacion`, `rut_cliente`) y a conciliación (`fecha` + `estado`).
- UI: design system del cliente, i18n, auditoría de acciones sensibles.
- Separar ambientes (`application-dev|prod`) y migraciones revisadas en PR.

### Índices (si existiera conciliación a 5M filas)

El reporte filtra por rango de `fecha_creacion` y agrupa por día / banco / estado. Con millones de filas:

1. **`(fecha_creacion)`** — ya existe (`ix_solicitud_fecha_creacion`): acota el rango.
2. **`(fecha_creacion, estado)`** — acelera los `SUM(CASE WHEN estado …)` del día.
3. **`(fecha_creacion, banco_destino)`** o **`(banco_destino)`** — ayuda al top 5 por monto en el rango.

Sin el filtro temporal indexado, PostgreSQL tiende a seq scan. Un `EXPLAIN ANALYZE` sobre el rango real de prod confirma si el plan usa Index Scan / Bitmap Heap Scan.

### Reporte de conciliación (Parte 5)

- `GET /api/v1/reportes/conciliacion?desde=YYYY-MM-DD&hasta=YYYY-MM-DD` (JWT).
- Agregación **en PostgreSQL** (`GROUP BY` día + top bancos); Java solo arma el DTO.
- Por día (`fecha_creacion`): total solicitado, aprobado (`APROBADA`+`PAGADA`), pagado, tasa de rechazo (conteo).
- Top 5 `banco_destino` por monto en el mismo rango.
- UI: `/reportes/conciliacion` (filtros desde/hasta + tablas).
