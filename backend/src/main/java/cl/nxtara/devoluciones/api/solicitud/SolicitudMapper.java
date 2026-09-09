package cl.nxtara.devoluciones.api.solicitud;

import cl.nxtara.devoluciones.api.solicitud.dto.EventoSolicitudResponse;
import cl.nxtara.devoluciones.api.solicitud.dto.SolicitudResponse;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudEntity;

public final class SolicitudMapper {

    private SolicitudMapper() {
    }

    public static SolicitudResponse toResponse(SolicitudEntity entity) {
        return new SolicitudResponse(
                entity.getId(),
                entity.getFolio(),
                entity.getRutCliente(),
                entity.getNombreCliente(),
                entity.getMonto(),
                entity.getMoneda(),
                entity.getBancoDestino(),
                entity.getCuentaDestino(),
                entity.getOrigen(),
                entity.getEstado(),
                entity.getMotivoRechazo(),
                entity.getReferenciaBanco(),
                entity.getReaperturas(),
                entity.getCreadaPor(),
                entity.getFechaCreacion(),
                entity.getActualizadaPor(),
                entity.getFechaActualizacion()
        );
    }

    public static EventoSolicitudResponse toEventoResponse(EventoSolicitudEntity entity) {
        return new EventoSolicitudResponse(
                entity.getId(),
                entity.getEstadoOrigen(),
                entity.getEstadoDestino(),
                entity.getUsuario(),
                entity.getFecha(),
                entity.getComentario()
        );
    }
}
