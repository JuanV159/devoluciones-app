package cl.nxtara.devoluciones.domain;

/**
 * Acciones de transición. No se muta el estado con un PUT genérico:
 * cada cambio de ciclo de vida es un comando explícito con reglas propias.
 */
public enum Accion {
    ENVIAR,
    APROBAR,
    RECHAZAR,
    PAGAR,
    REABRIR,
    ANULAR
}
