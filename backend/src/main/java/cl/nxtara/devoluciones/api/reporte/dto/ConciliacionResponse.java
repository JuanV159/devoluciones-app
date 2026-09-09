package cl.nxtara.devoluciones.api.reporte.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ConciliacionResponse(
        LocalDate desde,
        LocalDate hasta,
        List<ConciliacionDiaResponse> porDia,
        List<BancoTopResponse> topBancos
) {
}
