package cl.nxtara.devoluciones.domain.exception;

public class RecursoNoEncontradoException extends DominioException {

    public RecursoNoEncontradoException(String message) {
        super("RECURSO_NO_ENCONTRADO", message);
    }
}
