package cl.nxtara.devoluciones.api.auth.dto;

import cl.nxtara.devoluciones.domain.Rol;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") long expiresIn,
        Rol rol
) {
}
