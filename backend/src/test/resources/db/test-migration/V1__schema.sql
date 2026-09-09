-- Esquema DevolucionesApp (PostgreSQL 16)

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    rol             VARCHAR(20)  NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ANALISTA', 'SUPERVISOR'))
);

CREATE TABLE folio_secuencia (
    anio            INT PRIMARY KEY,
    ultimo_numero   INT NOT NULL
);

CREATE TABLE solicitud (
    id                   BIGSERIAL PRIMARY KEY,
    folio                VARCHAR(20)    NOT NULL,
    rut_cliente          VARCHAR(12)    NOT NULL,
    nombre_cliente       VARCHAR(200)   NOT NULL,
    monto                NUMERIC(12, 2) NOT NULL,
    moneda               VARCHAR(3)     NOT NULL DEFAULT 'CLP',
    banco_destino        VARCHAR(100)   NOT NULL,
    cuenta_destino       VARCHAR(50)    NOT NULL,
    origen               VARCHAR(20)    NOT NULL,
    estado               VARCHAR(20)    NOT NULL,
    motivo_rechazo       VARCHAR(500),
    referencia_banco     VARCHAR(100),
    reaperturas          INT            NOT NULL DEFAULT 0,
    creada_por           VARCHAR(50)    NOT NULL,
    fecha_creacion       TIMESTAMP WITH TIME ZONE    NOT NULL,
    actualizada_por      VARCHAR(50)    NOT NULL,
    fecha_actualizacion  TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT uq_solicitud_folio UNIQUE (folio),
    CONSTRAINT uq_solicitud_referencia_banco UNIQUE (referencia_banco),
    CONSTRAINT ck_solicitud_origen CHECK (origen IN ('MANUAL', 'CARGA_MASIVA')),
    CONSTRAINT ck_solicitud_estado CHECK (estado IN (
        'BORRADOR', 'EN_REVISION', 'APROBADA', 'RECHAZADA', 'PAGADA', 'ANULADA'
    )),
    CONSTRAINT ck_solicitud_monto CHECK (monto > 0 AND monto <= 10000000),
    CONSTRAINT ck_solicitud_reaperturas CHECK (reaperturas >= 0 AND reaperturas <= 1),
    CONSTRAINT ck_solicitud_motivo_rechazo CHECK (
        (estado = 'RECHAZADA' AND motivo_rechazo IS NOT NULL AND length(trim(motivo_rechazo)) > 0)
        OR (estado <> 'RECHAZADA')
    )
);

CREATE INDEX ix_solicitud_estado ON solicitud (estado);
CREATE INDEX ix_solicitud_rut ON solicitud (rut_cliente);
CREATE INDEX ix_solicitud_fecha_creacion ON solicitud (fecha_creacion);

CREATE TABLE evento_solicitud (
    id              BIGSERIAL PRIMARY KEY,
    solicitud_id    BIGINT       NOT NULL,
    estado_origen   VARCHAR(20),
    estado_destino  VARCHAR(20)  NOT NULL,
    usuario         VARCHAR(50)  NOT NULL,
    fecha           TIMESTAMP WITH TIME ZONE  NOT NULL,
    comentario      VARCHAR(500),
    CONSTRAINT fk_evento_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitud (id)
);

CREATE INDEX ix_evento_solicitud_id ON evento_solicitud (solicitud_id);
CREATE INDEX ix_evento_fecha ON evento_solicitud (fecha);
