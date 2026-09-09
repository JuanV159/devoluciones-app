package cl.nxtara.devoluciones.api.solicitud.dto;

import cl.nxtara.devoluciones.domain.Estado;

import java.time.Instant;

public record EventoSolicitudResponse(
        Long id,
        Estado estadoOrigen,
        Estado estadoDestino,
        String usuario,
        Instant fecha,
        String comentario
) {
}
