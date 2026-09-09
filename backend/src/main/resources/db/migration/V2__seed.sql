-- Seed: usuarios demo + 10 solicitudes con histórico coherente.
-- Password de ambos usuarios: Password123!

INSERT INTO usuario (username, password_hash, rol, activo) VALUES
('analista1', '$2a$10$oL1LvsyoQ3z6U4mUP2xDQu4klXBCf0KKybXUQ.jVUmKpBX6oNLkla', 'ANALISTA', TRUE),
('supervisor1', '$2a$10$oL1LvsyoQ3z6U4mUP2xDQu4klXBCf0KKybXUQ.jVUmKpBX6oNLkla', 'SUPERVISOR', TRUE);

INSERT INTO folio_secuencia (anio, ultimo_numero) VALUES (2026, 10);

-- 1 BORRADOR
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    1, 'DEV-2026-000001', '6876966-3', 'FELIPE SOTO MORALES', 150000.00, 'CLP',
    'BANCO ESTADO', '627820024292', 'MANUAL', 'BORRADOR', NULL, NULL, 0,
    'analista1', '2026-07-01 10:00:00+00', 'analista1', '2026-07-01 10:00:00+00'
);

-- 2 EN_REVISION
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    2, 'DEV-2026-000002', '15558837-3', 'VALENTINA TORRES LOPEZ', 320500.50, 'CLP',
    'ITAU', '289376437635', 'MANUAL', 'EN_REVISION', NULL, NULL, 0,
    'analista1', '2026-07-02 11:00:00+00', 'analista1', '2026-07-02 12:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(2, 'BORRADOR', 'EN_REVISION', 'analista1', '2026-07-02 12:00:00+00', 'ENVIAR');

-- 3 APROBADA
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    3, 'DEV-2026-000003', '9685216-9', 'FELIPE FLORES FLORES', 89000.00, 'CLP',
    'BANCO SECURITY', '644806247029', 'MANUAL', 'APROBADA', NULL, NULL, 0,
    'analista1', '2026-07-03 09:00:00+00', 'supervisor1', '2026-07-03 15:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(3, 'BORRADOR', 'EN_REVISION', 'analista1', '2026-07-03 10:00:00+00', 'ENVIAR'),
(3, 'EN_REVISION', 'APROBADA', 'supervisor1', '2026-07-03 15:00:00+00', 'APROBAR');

-- 4 RECHAZADA
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    4, 'DEV-2026-000004', '18401657-5', 'CRISTOBAL SILVA ROJAS', 450000.00, 'CLP',
    'BANCO ESTADO', '055785667210', 'MANUAL', 'RECHAZADA', 'Cuenta destino no coincide', NULL, 0,
    'analista1', '2026-07-04 09:00:00+00', 'supervisor1', '2026-07-04 16:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(4, 'BORRADOR', 'EN_REVISION', 'analista1', '2026-07-04 10:00:00+00', 'ENVIAR'),
(4, 'EN_REVISION', 'RECHAZADA', 'supervisor1', '2026-07-04 16:00:00+00', 'Cuenta destino no coincide');

-- 5 PAGADA
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    5, 'DEV-2026-000005', '8679214-1', 'FRANCISCA DIAZ FUENTES', 1250000.00, 'CLP',
    'BANCO SECURITY', '655474071330', 'MANUAL', 'PAGADA', NULL, NULL, 0,
    'analista1', '2026-07-05 08:00:00+00', 'supervisor1', '2026-07-05 18:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(5, 'BORRADOR', 'EN_REVISION', 'analista1', '2026-07-05 09:00:00+00', 'ENVIAR'),
(5, 'EN_REVISION', 'APROBADA', 'supervisor1', '2026-07-05 14:00:00+00', 'APROBAR'),
(5, 'APROBADA', 'PAGADA', 'supervisor1', '2026-07-05 18:00:00+00', 'PAGAR');

-- 6 ANULADA
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    6, 'DEV-2026-000006', '20705148-9', 'FERNANDA MARTINEZ FLORES', 75000.00, 'CLP',
    'BANCO ESTADO', '974590532631', 'MANUAL', 'ANULADA', NULL, NULL, 0,
    'analista1', '2026-07-06 10:00:00+00', 'analista1', '2026-07-06 11:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(6, 'BORRADOR', 'ANULADA', 'analista1', '2026-07-06 11:00:00+00', 'ANULAR');

-- 7 BORRADOR tras reapertura (reaperturas = 1)
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    7, 'DEV-2026-000007', '23017734-1', 'ANTONIA MORALES MUNOZ', 210000.00, 'CLP',
    'BANCO SANTANDER', '002948728483', 'MANUAL', 'BORRADOR', NULL, NULL, 1,
    'analista1', '2026-07-07 09:00:00+00', 'analista1', '2026-07-07 17:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(7, 'BORRADOR', 'EN_REVISION', 'analista1', '2026-07-07 10:00:00+00', 'ENVIAR'),
(7, 'EN_REVISION', 'RECHAZADA', 'supervisor1', '2026-07-07 15:00:00+00', 'Monto no justificado'),
(7, 'RECHAZADA', 'BORRADOR', 'analista1', '2026-07-07 17:00:00+00', 'REABRIR');

-- 8 EN_REVISION (carga masiva)
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    8, 'DEV-2026-000008', '13837869-1', 'FERNANDA DIAZ ARAYA', 999999.99, 'CLP',
    'SCOTIABANK', '704694611575', 'CARGA_MASIVA', 'EN_REVISION', NULL, 'REF-SEED-000008', 0,
    'analista1', '2026-07-08 08:30:00+00', 'analista1', '2026-07-08 08:30:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(8, NULL, 'EN_REVISION', 'analista1', '2026-07-08 08:30:00+00', 'Alta por carga masiva');

-- 9 APROBADA
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    9, 'DEV-2026-000009', '22034339-1', 'JORGE CONTRERAS ROJAS', 560000.00, 'CLP',
    'BANCO SANTANDER', '658132452561', 'MANUAL', 'APROBADA', NULL, NULL, 0,
    'analista1', '2026-07-09 09:00:00+00', 'supervisor1', '2026-07-09 14:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(9, 'BORRADOR', 'EN_REVISION', 'analista1', '2026-07-09 10:00:00+00', 'ENVIAR'),
(9, 'EN_REVISION', 'APROBADA', 'supervisor1', '2026-07-09 14:00:00+00', 'APROBAR');

-- 10 PAGADA
INSERT INTO solicitud (
    id, folio, rut_cliente, nombre_cliente, monto, moneda, banco_destino, cuenta_destino,
    origen, estado, motivo_rechazo, referencia_banco, reaperturas,
    creada_por, fecha_creacion, actualizada_por, fecha_actualizacion
) VALUES (
    10, 'DEV-2026-000010', '15876872-0', 'NICOLAS PEREZ MUNOZ', 1800000.00, 'CLP',
    'SCOTIABANK', '062157982007', 'MANUAL', 'PAGADA', NULL, NULL, 0,
    'analista1', '2026-07-10 08:00:00+00', 'supervisor1', '2026-07-10 19:00:00+00'
);
INSERT INTO evento_solicitud (solicitud_id, estado_origen, estado_destino, usuario, fecha, comentario) VALUES
(10, 'BORRADOR', 'EN_REVISION', 'analista1', '2026-07-10 09:00:00+00', 'ENVIAR'),
(10, 'EN_REVISION', 'APROBADA', 'supervisor1', '2026-07-10 13:00:00+00', 'APROBAR'),
(10, 'APROBADA', 'PAGADA', 'supervisor1', '2026-07-10 19:00:00+00', 'PAGAR');

SELECT setval('solicitud_id_seq', (SELECT MAX(id) FROM solicitud));
SELECT setval('evento_solicitud_id_seq', (SELECT MAX(id) FROM evento_solicitud));
SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM usuario));
