package cl.nxtara.devoluciones.domain.exception;

/**
 * El rol del usuario no alcanza para la acción (R2).
 * En la capa HTTP se mapeará a 403 Forbidden.
 */
public class AccesoDenegadoException extends DominioException {

    public AccesoDenegadoException(String message) {
        super("ACCESO_DENEGADO", message);
    }
}
