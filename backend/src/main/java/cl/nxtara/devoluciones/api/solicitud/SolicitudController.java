package cl.nxtara.devoluciones.api.solicitud;

import cl.nxtara.devoluciones.api.solicitud.dto.EventoSolicitudResponse;
import cl.nxtara.devoluciones.api.solicitud.dto.RechazoRequest;
import cl.nxtara.devoluciones.api.solicitud.dto.SolicitudRequest;
import cl.nxtara.devoluciones.api.solicitud.dto.SolicitudResponse;
import cl.nxtara.devoluciones.application.SolicitudService;
import cl.nxtara.devoluciones.application.UsuarioActualProvider;
import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.domain.Origen;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final UsuarioActualProvider usuarioActualProvider;

    public SolicitudController(SolicitudService solicitudService, UsuarioActualProvider usuarioActualProvider) {
        this.solicitudService = solicitudService;
        this.usuarioActualProvider = usuarioActualProvider;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request) {
        SolicitudResponse creada = solicitudService.crear(request, usuarioActualProvider.require());
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
            @Valid @RequestBody SolicitudRequest request
    ) {
        return solicitudService.actualizar(id, request, usuarioActualProvider.require());
    }

    @PostMapping("/{id}/enviar")
    public SolicitudResponse enviar(@PathVariable Long id) {
        return solicitudService.enviar(id, usuarioActualProvider.require());
    }

    @PostMapping("/{id}/aprobar")
    public SolicitudResponse aprobar(@PathVariable Long id) {
        return solicitudService.aprobar(id, usuarioActualProvider.require());
    }

    @PostMapping("/{id}/rechazar")
    public SolicitudResponse rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazoRequest request
    ) {
        return solicitudService.rechazar(id, request.motivoRechazo(), usuarioActualProvider.require());
    }

    @PostMapping("/{id}/pagar")
    public SolicitudResponse pagar(@PathVariable Long id) {
        return solicitudService.pagar(id, usuarioActualProvider.require());
    }

    @PostMapping("/{id}/reabrir")
    public SolicitudResponse reabrir(@PathVariable Long id) {
        return solicitudService.reabrir(id, usuarioActualProvider.require());
    }

    @PostMapping("/{id}/anular")
    public SolicitudResponse anular(@PathVariable Long id) {
        return solicitudService.anular(id, usuarioActualProvider.require());
    }

    @GetMapping("/{id}/historial")
    public List<EventoSolicitudResponse> historial(@PathVariable Long id) {
        return solicitudService.historial(id);
    }
}
