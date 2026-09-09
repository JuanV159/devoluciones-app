package cl.nxtara.devoluciones.domain.exception;

public abstract class DominioException extends RuntimeException {

    private final String codigo;

    protected DominioException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
