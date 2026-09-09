-- Cargas masivas CSV

CREATE TABLE carga (
    id               BIGSERIAL PRIMARY KEY,
    nombre_archivo   VARCHAR(255) NOT NULL,
    estado           VARCHAR(20)  NOT NULL,
    total_filas      INT          NOT NULL DEFAULT 0,
    filas_ok         INT          NOT NULL DEFAULT 0,
    filas_rechazadas INT          NOT NULL DEFAULT 0,
    filas_omitidas   INT          NOT NULL DEFAULT 0,
    creada_por       VARCHAR(50)  NOT NULL,
    fecha_creacion   TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_fin        TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_carga_estado CHECK (estado IN ('PROCESANDO', 'COMPLETADA', 'FALLIDA'))
);

CREATE TABLE carga_error (
    id                BIGSERIAL PRIMARY KEY,
    carga_id          BIGINT       NOT NULL,
    fila              INT          NOT NULL,
    campo             VARCHAR(50),
    motivo            VARCHAR(500) NOT NULL,
    referencia_banco  VARCHAR(100),
    CONSTRAINT fk_carga_error_carga FOREIGN KEY (carga_id) REFERENCES carga (id)
);

CREATE INDEX ix_carga_error_carga ON carga_error (carga_id);
