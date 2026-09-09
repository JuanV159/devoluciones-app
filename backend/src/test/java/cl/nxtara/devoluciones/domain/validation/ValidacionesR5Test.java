package cl.nxtara.devoluciones.domain.validation;

import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validaciones R5 — RUT y monto")
class ValidacionesR5Test {

    @ParameterizedTest(name = "RUT válido: {0}")
    @ValueSource(strings = {
            "12345678-5",
            "11111111-1",
            "6876966-3",
            "25718501-K",
            "25718501-k"
    })
    void rutValidos(String rut) {
        assertTrue(RutChilenoValidator.esValido(rut));
    }

    @Test
    void digitoVerificadorKSeCalculaCorrectamente() {
        assertEquals('K', RutChilenoValidator.calcularDv("25718501"));
        assertTrue(RutChilenoValidator.esValido("25718501-K"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "123",
            "19350791-9",
            "12767053-4",
            "12.345.678-5",
            "ABCDEFGH-1"
    })
    void rutInvalidos(String rut) {
        assertFalse(RutChilenoValidator.esValido(rut));
    }

    @Test
    void validarOFallarLanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> RutChilenoValidator.validarOFallar("19350791-9"));
    }

    @ParameterizedTest
    @CsvSource({
            "0.01, 0.01",
            "150000.00, 150000.00",
            "10000000.00, 10000000.00",
            "100, 100.00"
    })
    void montosValidos(String entrada, String esperado) {
        assertEquals(new BigDecimal(esperado), MontoValidator.normalizarYValidar(new BigDecimal(entrada)));
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "-1",
            "10000000.01",
            "42713009.00"
    })
    void montosInvalidos(String entrada) {
        assertThrows(
                ReglaNegocioException.class,
                () -> MontoValidator.normalizarYValidar(new BigDecimal(entrada))
        );
    }

    @Test
    void montoConMasDeDosDecimalesFalla() {
        assertThrows(
                ReglaNegocioException.class,
                () -> MontoValidator.normalizarYValidar(new BigDecimal("10.123"))
        );
    }
}
