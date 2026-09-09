package cl.nxtara.devoluciones.domain.exception;

/**
 * Transición no permitida por el estado actual o por una regla de ciclo de vida (R1, R4, R7).
 * En la capa HTTP se mapeará a 409 Conflict.
 */
public class TransicionInvalidaException extends DominioException {

    public TransicionInvalidaException(String message) {
        super("TRANSICION_INVALIDA", message);
    }
}
