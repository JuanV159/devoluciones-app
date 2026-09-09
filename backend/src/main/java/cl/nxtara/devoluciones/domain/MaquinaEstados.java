package cl.nxtara.devoluciones.domain;

import cl.nxtara.devoluciones.domain.exception.AccesoDenegadoException;
import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;
import cl.nxtara.devoluciones.domain.exception.TransicionInvalidaException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Pieza cohesiva de ciclo de vida (R1–R7).
 * Un estado nuevo mañana se agrega aquí, no repartido en ifs del service.
 */
public final class MaquinaEstados {

    private static final Map<Estado, Map<Accion, Estado>> TRANSICIONES = new EnumMap<>(Estado.class);

    /** Según R2: solo estas acciones exigen SUPERVISOR. */
    private static final Set<Accion> ACCIONES_SUPERVISOR = Set.of(
            Accion.APROBAR,
            Accion.RECHAZAR,
            Accion.PAGAR
    );

    static {
        registrar(Estado.BORRADOR, Accion.ENVIAR, Estado.EN_REVISION);
        registrar(Estado.BORRADOR, Accion.ANULAR, Estado.ANULADA);
        registrar(Estado.EN_REVISION, Accion.APROBAR, Estado.APROBADA);
        registrar(Estado.EN_REVISION, Accion.RECHAZAR, Estado.RECHAZADA);
        registrar(Estado.APROBADA, Accion.PAGAR, Estado.PAGADA);
        registrar(Estado.RECHAZADA, Accion.REABRIR, Estado.BORRADOR);
    }

    private MaquinaEstados() {
    }

    private static void registrar(Estado origen, Accion accion, Estado destino) {
        TRANSICIONES
                .computeIfAbsent(origen, ignored -> new EnumMap<>(Accion.class))
                .put(accion, destino);
    }

    public static TransicionResultado transicionar(Estado estadoActual, Accion accion, TransicionContexto contexto) {
        if (estadoActual == null) {
            throw new IllegalArgumentException("estadoActual es obligatorio");
        }
        if (accion == null) {
            throw new IllegalArgumentException("accion es obligatoria");
        }
        if (contexto == null) {
            throw new IllegalArgumentException("contexto es obligatorio");
        }

        validarRol(accion, contexto.rol());

        Estado destino = TRANSICIONES
                .getOrDefault(estadoActual, Map.of())
                .get(accion);

        if (destino == null) {
            throw new TransicionInvalidaException(
                    "No se puede aplicar la acción " + accion + " cuando la solicitud está en " + estadoActual
            );
        }

        if (accion == Accion.RECHAZAR) {
            validarMotivoRechazo(contexto.motivoRechazo());
        }

        if (accion == Accion.REABRIR) {
            validarReapertura(contexto.reaperturas());
        }

        if (accion == Accion.APROBAR) {
            validarSeparacionDeFunciones(contexto);
        }

        int reaperturas = contexto.reaperturas();
        if (accion == Accion.REABRIR) {
            reaperturas = reaperturas + 1;
        }

        String comentario = resolverComentario(accion, contexto);

        return new TransicionResultado(
                estadoActual,
                destino,
                accion,
                contexto.usuario(),
                comentario,
                accion == Accion.RECHAZAR ? contexto.motivoRechazo().trim() : null,
                reaperturas
        );
    }

    private static void validarRol(Accion accion, Rol rol) {
        if (ACCIONES_SUPERVISOR.contains(accion) && rol != Rol.SUPERVISOR) {
            throw new AccesoDenegadoException(
                    "La acción " + accion + " requiere rol SUPERVISOR"
            );
        }
    }

    private static void validarMotivoRechazo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("El motivo de rechazo es obligatorio");
        }
    }

    private static void validarReapertura(int reaperturas) {
        // R4: contador en la solicitud (O(1)); el EventoSolicitud queda como evidencia.
        if (reaperturas >= 1) {
            throw new TransicionInvalidaException(
                    "La solicitud ya fue reabierta una vez; no es posible reabrir nuevamente"
            );
        }
    }

    private static void validarSeparacionDeFunciones(TransicionContexto contexto) {
        if (contexto.usuario().equalsIgnoreCase(contexto.creadaPor())) {
            throw new TransicionInvalidaException(
                    "El supervisor que aprueba no puede ser el mismo usuario que creó la solicitud"
            );
        }
    }

    private static String resolverComentario(Accion accion, TransicionContexto contexto) {
        if (accion == Accion.RECHAZAR) {
            return contexto.motivoRechazo().trim();
        }
        if (contexto.comentario() != null && !contexto.comentario().isBlank()) {
            return contexto.comentario().trim();
        }
        return accion.name();
    }
}
