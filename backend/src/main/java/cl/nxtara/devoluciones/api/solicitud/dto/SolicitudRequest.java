package cl.nxtara.devoluciones.api.solicitud.dto;

import cl.nxtara.devoluciones.api.validation.RutChileno;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SolicitudRequest(
        @NotBlank @RutChileno String rutCliente,
        @NotBlank String nombreCliente,
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        @DecimalMax(value = "10000000.00", inclusive = true)
        @Digits(integer = 10, fraction = 2)
        BigDecimal monto,
        @NotBlank String bancoDestino,
        @NotBlank String cuentaDestino
) {
}
