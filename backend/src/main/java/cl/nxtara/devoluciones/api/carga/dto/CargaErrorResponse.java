package cl.nxtara.devoluciones.api.carga.dto;

public record CargaErrorResponse(
        int fila,
        String campo,
        String motivo,
        String referenciaBanco
) {
}
