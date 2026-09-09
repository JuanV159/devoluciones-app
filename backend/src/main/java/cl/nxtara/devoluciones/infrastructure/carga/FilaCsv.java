package cl.nxtara.devoluciones.infrastructure.carga;

import java.util.Optional;

/**
 * Fila cruda del CSV (número de línea en archivo, 1 = encabezado).
 */
public record FilaCsv(
        int numeroFila,
        Optional<String> rutCliente,
        Optional<String> nombreCliente,
        Optional<String> monto,
        Optional<String> bancoDestino,
        Optional<String> cuentaDestino,
        Optional<String> referenciaBanco,
        boolean incompleta
) {
}
