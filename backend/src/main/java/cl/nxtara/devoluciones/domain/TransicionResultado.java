package cl.nxtara.devoluciones.domain;

/**
 * Resultado puro de una transición válida.
 * La capa de aplicación debe persistir solicitud + EventoSolicitud en la misma transacción (R6).
 */
public record TransicionResultado(
        Estado estadoOrigen,
        Estado estadoDestino,
        Accion accion,
        String usuario,
        String comentario,
        String motivoRechazo,
        int reaperturas
) {
}
