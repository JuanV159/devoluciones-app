package cl.nxtara.devoluciones.api.reporte.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConciliacionDiaResponse(
        LocalDate fecha,
        BigDecimal totalSolicitado,
        BigDecimal totalAprobado,
        BigDecimal totalPagado,
        BigDecimal tasaRechazo
) {
}
