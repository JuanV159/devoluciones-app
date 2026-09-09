package cl.nxtara.devoluciones.infrastructure.persistence;

import cl.nxtara.devoluciones.api.reporte.dto.BancoTopResponse;
import cl.nxtara.devoluciones.api.reporte.dto.ConciliacionDiaResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Agregaciones de conciliación en SQL (GROUP BY), no en memoria Java.
 * Usa CASE WHEN (portable H2 MODE=PostgreSQL y PostgreSQL 16).
 */
@Repository
public class ConciliacionQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConciliacionQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ConciliacionDiaResponse> agregarPorDia(Instant desdeInclusive, Instant hastaExclusive) {
        String sql = """
                SELECT CAST(fecha_creacion AS DATE) AS dia,
                       COALESCE(SUM(monto), 0) AS total_solicitado,
                       COALESCE(SUM(CASE WHEN estado IN ('APROBADA', 'PAGADA') THEN monto ELSE 0 END), 0) AS total_aprobado,
                       COALESCE(SUM(CASE WHEN estado = 'PAGADA' THEN monto ELSE 0 END), 0) AS total_pagado,
                       COUNT(*) AS cantidad_total,
                       SUM(CASE WHEN estado = 'RECHAZADA' THEN 1 ELSE 0 END) AS cantidad_rechazada
                FROM solicitud
                WHERE fecha_creacion >= ? AND fecha_creacion < ?
                GROUP BY CAST(fecha_creacion AS DATE)
                ORDER BY dia
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LocalDate fecha = rs.getObject("dia", LocalDate.class);
            if (fecha == null) {
                Date legacy = rs.getDate("dia");
                fecha = legacy != null ? legacy.toLocalDate() : null;
            }
            BigDecimal solicitado = rs.getBigDecimal("total_solicitado");
            BigDecimal aprobado = rs.getBigDecimal("total_aprobado");
            BigDecimal pagado = rs.getBigDecimal("total_pagado");
            long total = rs.getLong("cantidad_total");
            long rechazadas = rs.getLong("cantidad_rechazada");
            BigDecimal tasa = total == 0
                    ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
                    : BigDecimal.valueOf(rechazadas)
                    .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
            return new ConciliacionDiaResponse(fecha, solicitado, aprobado, pagado, tasa);
        }, Timestamp.from(desdeInclusive), Timestamp.from(hastaExclusive));
    }

    public List<BancoTopResponse> topBancos(Instant desdeInclusive, Instant hastaExclusive, int limite) {
        String sql = """
                SELECT banco_destino,
                       COALESCE(SUM(monto), 0) AS monto_total
                FROM solicitud
                WHERE fecha_creacion >= ? AND fecha_creacion < ?
                GROUP BY banco_destino
                ORDER BY monto_total DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new BancoTopResponse(
                        rs.getString("banco_destino"),
                        rs.getBigDecimal("monto_total")
                ),
                Timestamp.from(desdeInclusive),
                Timestamp.from(hastaExclusive),
                limite
        );
    }
}
