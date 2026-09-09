package cl.nxtara.devoluciones.domain.validation;

import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Reglas de monto CLP (R5): &gt; 0 y ≤ 10.000.000, máximo 2 decimales.
 */
public final class MontoValidator {

    public static final BigDecimal MONTO_MAXIMO = new BigDecimal("10000000.00");

    private MontoValidator() {
    }

    public static BigDecimal normalizarYValidar(BigDecimal monto) {
        if (monto == null) {
            throw new ReglaNegocioException("El monto es obligatorio");
        }
        if (monto.scale() > 2) {
            throw new ReglaNegocioException("El monto admite como máximo 2 decimales");
        }
        BigDecimal normalizado = monto.setScale(2, RoundingMode.UNNECESSARY);
        if (normalizado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor que 0");
        }
        if (normalizado.compareTo(MONTO_MAXIMO) > 0) {
            throw new ReglaNegocioException("El monto no puede superar 10.000.000 CLP");
        }
        return normalizado;
    }
}
