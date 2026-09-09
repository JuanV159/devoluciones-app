package cl.nxtara.devoluciones.application;

import cl.nxtara.devoluciones.api.solicitud.SolicitudMapper;
import cl.nxtara.devoluciones.api.solicitud.dto.EventoSolicitudResponse;
import cl.nxtara.devoluciones.api.solicitud.dto.SolicitudRequest;
import cl.nxtara.devoluciones.api.solicitud.dto.SolicitudResponse;
import cl.nxtara.devoluciones.domain.Accion;
import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.domain.MaquinaEstados;
import cl.nxtara.devoluciones.domain.Origen;
import cl.nxtara.devoluciones.domain.TransicionContexto;
import cl.nxtara.devoluciones.domain.TransicionResultado;
import cl.nxtara.devoluciones.domain.exception.RecursoNoEncontradoException;
import cl.nxtara.devoluciones.domain.exception.TransicionInvalidaException;
import cl.nxtara.devoluciones.domain.validation.MontoValidator;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final EventoSolicitudRepository eventoSolicitudRepository;
    private final FolioService folioService;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            EventoSolicitudRepository eventoSolicitudRepository,
            FolioService folioService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.eventoSolicitudRepository = eventoSolicitudRepository;
        this.folioService = folioService;
    }

    @Transactional
    public SolicitudResponse crear(SolicitudRequest request, UsuarioActual usuario) {
        Instant ahora = Instant.now();
        SolicitudEntity entity = new SolicitudEntity();
        entity.setFolio(folioService.siguienteFolio());
        aplicarDatosEditables(entity, request);
        entity.setMoneda("CLP");
        entity.setOrigen(Origen.MANUAL);
        entity.setEstado(Estado.BORRADOR);
        entity.setReaperturas(0);
        entity.setCreadaPor(usuario.username());
        entity.setFechaCreacion(ahora);
        entity.setActualizadaPor(usuario.username());
        entity.setFechaActualizacion(ahora);
        return SolicitudMapper.toResponse(solicitudRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id) {
        return SolicitudMapper.toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public Page<SolicitudResponse> listar(
            Estado estado,
            String rutCliente,
            Origen origen,
            Instant desde,
            Instant hasta,
            Pageable pageable
    ) {
        return solicitudRepository
                .findAll(SolicitudSpecifications.conFiltros(estado, rutCliente, origen, desde, hasta), pageable)
                .map(SolicitudMapper::toResponse);
    }

    @Transactional
    public SolicitudResponse actualizar(Long id, SolicitudRequest request, UsuarioActual usuario) {
        SolicitudEntity entity = buscar(id);
        if (entity.getEstado() != Estado.BORRADOR) {
            throw new TransicionInvalidaException(
                    "Solo se puede editar una solicitud en estado BORRADOR (actual: " + entity.getEstado() + ")"
            );
        }
        aplicarDatosEditables(entity, request);
        entity.setActualizadaPor(usuario.username());
        entity.setFechaActualizacion(Instant.now());
        return SolicitudMapper.toResponse(solicitudRepository.save(entity));
    }

    @Transactional
    public SolicitudResponse enviar(Long id, UsuarioActual usuario) {
        return aplicarTransicion(id, Accion.ENVIAR, usuario, null);
    }

    @Transactional
    public SolicitudResponse aprobar(Long id, UsuarioActual usuario) {
        return aplicarTransicion(id, Accion.APROBAR, usuario, null);
    }

    @Transactional
    public SolicitudResponse rechazar(Long id, String motivoRechazo, UsuarioActual usuario) {
        return aplicarTransicion(id, Accion.RECHAZAR, usuario, motivoRechazo);
    }

    @Transactional
    public SolicitudResponse pagar(Long id, UsuarioActual usuario) {
        return aplicarTransicion(id, Accion.PAGAR, usuario, null);
    }

    @Transactional
    public SolicitudResponse reabrir(Long id, UsuarioActual usuario) {
        return aplicarTransicion(id, Accion.REABRIR, usuario, null);
    }

    @Transactional
    public SolicitudResponse anular(Long id, UsuarioActual usuario) {
        return aplicarTransicion(id, Accion.ANULAR, usuario, null);
    }

    @Transactional(readOnly = true)
    public List<EventoSolicitudResponse> historial(Long id) {
        buscar(id);
        return eventoSolicitudRepository.findBySolicitudIdOrderByFechaAsc(id).stream()
                .map(SolicitudMapper::toEventoResponse)
                .toList();
    }

    private SolicitudResponse aplicarTransicion(
            Long id,
            Accion accion,
            UsuarioActual usuario,
            String motivoRechazo
    ) {
        SolicitudEntity entity = buscar(id);
        TransicionContexto contexto = TransicionContexto
                .de(usuario.username(), usuario.rol(), entity.getCreadaPor(), entity.getReaperturas())
                .conMotivoRechazo(motivoRechazo);

        TransicionResultado resultado = MaquinaEstados.transicionar(entity.getEstado(), accion, contexto);

        entity.setEstado(resultado.estadoDestino());
        entity.setReaperturas(resultado.reaperturas());
        entity.setMotivoRechazo(resultado.motivoRechazo());
        if (accion == Accion.REABRIR) {
            entity.setMotivoRechazo(null);
        }
        entity.setActualizadaPor(usuario.username());
        entity.setFechaActualizacion(Instant.now());
        solicitudRepository.save(entity);

        EventoSolicitudEntity evento = new EventoSolicitudEntity();
        evento.setSolicitud(entity);
        evento.setEstadoOrigen(resultado.estadoOrigen());
        evento.setEstadoDestino(resultado.estadoDestino());
        evento.setUsuario(resultado.usuario());
        evento.setFecha(Instant.now());
        evento.setComentario(resultado.comentario());
        eventoSolicitudRepository.save(evento);

        return SolicitudMapper.toResponse(entity);
    }

    private void aplicarDatosEditables(SolicitudEntity entity, SolicitudRequest request) {
        entity.setRutCliente(request.rutCliente().trim().toUpperCase());
        entity.setNombreCliente(request.nombreCliente().trim());
        entity.setMonto(MontoValidator.normalizarYValidar(request.monto()));
        entity.setBancoDestino(request.bancoDestino().trim());
        entity.setCuentaDestino(request.cuentaDestino().trim());
    }

    private SolicitudEntity buscar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + id));
    }
}
