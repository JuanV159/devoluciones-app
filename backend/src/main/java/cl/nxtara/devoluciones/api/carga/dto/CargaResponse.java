package cl.nxtara.devoluciones.api.carga.dto;

import cl.nxtara.devoluciones.domain.EstadoCarga;

import java.time.Instant;
import java.util.List;

public record CargaResponse(
        Long id,
        String nombreArchivo,
        EstadoCarga estado,
        int totalFilas,
        int filasOk,
        int filasRechazadas,
        int filasOmitidas,
        String creadaPor,
        Instant fechaCreacion,
        Instant fechaFin,
        List<CargaErrorResponse> errores
) {
}
