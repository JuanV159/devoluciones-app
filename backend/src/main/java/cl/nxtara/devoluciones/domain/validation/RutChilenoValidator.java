package cl.nxtara.devoluciones.domain.validation;

import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validador de RUT chileno (módulo 11). Acepta DV numérico o K (R5).
 */
public final class RutChilenoValidator {

    private static final Pattern FORMATO = Pattern.compile("^(\\d{7,8})-([\\dKk])$");

    private RutChilenoValidator() {
    }

    public static boolean esValido(String rut) {
        if (rut == null || rut.isBlank()) {
            return false;
        }
        String normalizado = rut.trim().toUpperCase(Locale.ROOT);
        var matcher = FORMATO.matcher(normalizado);
        if (!matcher.matches()) {
            return false;
        }
        String cuerpo = matcher.group(1);
        char dvInformado = matcher.group(2).charAt(0);
        return calcularDv(cuerpo) == dvInformado;
    }

    public static void validarOFallar(String rut) {
        if (!esValido(rut)) {
            throw new ReglaNegocioException("RUT inválido: " + rut);
        }
    }

    public static char calcularDv(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return Character.forDigit(resto, 10);
    }
}
