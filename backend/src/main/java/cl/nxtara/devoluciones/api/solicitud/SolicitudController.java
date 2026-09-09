package cl.nxtara.devoluciones.api.solicitud;

import cl.nxtara.devoluciones.api.solicitud.dto.EventoSolicitudResponse;
import cl.nxtara.devoluciones.api.solicitud.dto.RechazoRequest;
import cl.nxtara.devoluciones.api.solicitud.dto.SolicitudRequest;
import cl.nxtara.devoluciones.api.solicitud.dto.SolicitudResponse;
import cl.nxtara.devoluciones.application.SolicitudService;
import cl.nxtara.devoluciones.application.UsuarioActual;
import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.domain.Origen;
import cl.nxtara.devoluciones.domain.Rol;
import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Contrato Parte 1. Identidad temporal vía headers X-Usuario / X-Rol
 * (se reemplaza por JWT en Parte 4).
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(
            @Valid @RequestBody SolicitudRequest request,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        SolicitudResponse creada = solicitudService.crear(request, usuarioActual(usuario, rol));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @GetMapping
    public Page<SolicitudResponse> listar(
            @RequestParam(required = false) Estado estado,
            @RequestParam(required = false) String rut,
            @RequestParam(required = false) Origen origen,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            Pageable pageable
    ) {
        return solicitudService.listar(estado, rut, origen, desde, hasta, pageable);
    }

    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable Long id) {
        return solicitudService.obtener(id);
    }

    @PutMapping("/{id}")
    public SolicitudResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRequest request,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        return solicitudService.actualizar(id, request, usuarioActual(usuario, rol));
    }

    @PostMapping("/{id}/enviar")
    public SolicitudResponse enviar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        return solicitudService.enviar(id, usuarioActual(usuario, rol));
    }

    @PostMapping("/{id}/aprobar")
    public SolicitudResponse aprobar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        return solicitudService.aprobar(id, usuarioActual(usuario, rol));
    }

    @PostMapping("/{id}/rechazar")
    public SolicitudResponse rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazoRequest request,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        return solicitudService.rechazar(id, request.motivoRechazo(), usuarioActual(usuario, rol));
    }

    @PostMapping("/{id}/pagar")
    public SolicitudResponse pagar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        return solicitudService.pagar(id, usuarioActual(usuario, rol));
    }

    @PostMapping("/{id}/reabrir")
    public SolicitudResponse reabrir(
            @PathVariable Long id,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        return solicitudService.reabrir(id, usuarioActual(usuario, rol));
    }

    @PostMapping("/{id}/anular")
    public SolicitudResponse anular(
            @PathVariable Long id,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        return solicitudService.anular(id, usuarioActual(usuario, rol));
    }

    @GetMapping("/{id}/historial")
    public List<EventoSolicitudResponse> historial(@PathVariable Long id) {
        return solicitudService.historial(id);
    }

    private UsuarioActual usuarioActual(String usuario, String rolRaw) {
        try {
            return new UsuarioActual(usuario.trim(), Rol.valueOf(rolRaw.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("Rol inválido: " + rolRaw + " (use ANALISTA o SUPERVISOR)");
        }
    }
}
