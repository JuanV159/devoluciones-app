-- Esquema inicial DevolucionesApp (placeholder del esqueleto).
-- Las tablas de dominio se agregan en commits siguientes.

CREATE TABLE IF NOT EXISTS schema_bootstrap (
    id BOOLEAN PRIMARY KEY DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
