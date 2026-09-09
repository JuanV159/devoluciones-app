package cl.nxtara.devoluciones.api.reporte.dto;

import java.math.BigDecimal;

public record BancoTopResponse(
        String bancoDestino,
        BigDecimal montoTotal
) {
}
