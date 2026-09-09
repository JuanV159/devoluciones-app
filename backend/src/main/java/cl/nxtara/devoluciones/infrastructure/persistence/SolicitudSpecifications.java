package cl.nxtara.devoluciones.infrastructure.persistence;

import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.domain.Origen;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class SolicitudSpecifications {

    private SolicitudSpecifications() {
    }

    public static Specification<SolicitudEntity> conFiltros(
            Estado estado,
            String rutCliente,
            Origen origen,
            Instant desde,
            Instant hasta
    ) {
        return Specification
                .where(estadoEquals(estado))
                .and(rutEquals(rutCliente))
                .and(origenEquals(origen))
                .and(fechaDesde(desde))
                .and(fechaHasta(hasta));
    }

    private static Specification<SolicitudEntity> estadoEquals(Estado estado) {
        return (root, query, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado);
    }

    private static Specification<SolicitudEntity> rutEquals(String rutCliente) {
        return (root, query, cb) ->
                rutCliente == null || rutCliente.isBlank()
                        ? null
                        : cb.equal(root.get("rutCliente"), rutCliente.trim());
    }

    private static Specification<SolicitudEntity> origenEquals(Origen origen) {
        return (root, query, cb) -> origen == null ? null : cb.equal(root.get("origen"), origen);
    }

    private static Specification<SolicitudEntity> fechaDesde(Instant desde) {
        return (root, query, cb) -> desde == null ? null : cb.greaterThanOrEqualTo(root.get("fechaCreacion"), desde);
    }

    private static Specification<SolicitudEntity> fechaHasta(Instant hasta) {
        return (root, query, cb) -> hasta == null ? null : cb.lessThanOrEqualTo(root.get("fechaCreacion"), hasta);
    }
}
