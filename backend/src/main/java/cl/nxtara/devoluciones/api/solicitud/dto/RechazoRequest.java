package cl.nxtara.devoluciones.api.solicitud.dto;

import jakarta.validation.constraints.NotBlank;

public record RechazoRequest(
        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String motivoRechazo
) {
}
