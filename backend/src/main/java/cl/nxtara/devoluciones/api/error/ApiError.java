package cl.nxtara.devoluciones.api.error;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String detalle,
        String path
) {
}
