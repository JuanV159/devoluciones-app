package cl.nxtara.devoluciones.api.solicitud.dto;

import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.domain.Origen;

import java.math.BigDecimal;
import java.time.Instant;

public record SolicitudResponse(
        Long id,
        String folio,
        String rutCliente,
        String nombreCliente,
        BigDecimal monto,
        String moneda,
        String bancoDestino,
        String cuentaDestino,
        Origen origen,
        Estado estado,
        String motivoRechazo,
        String referenciaBanco,
        int reaperturas,
        String creadaPor,
        Instant fechaCreacion,
        String actualizadaPor,
        Instant fechaActualizacion
) {
}
