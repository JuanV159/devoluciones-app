package cl.nxtara.devoluciones.domain.exception;

/**
 * Violación de validación de entrada / regla de datos (R3, R5).
 * En la capa HTTP se mapeará a 400 Bad Request.
 */
public class ReglaNegocioException extends DominioException {

    public ReglaNegocioException(String message) {
        super("REGLA_NEGOCIO", message);
    }
}
