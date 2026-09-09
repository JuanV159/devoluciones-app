package cl.nxtara.devoluciones.api.carga;

import cl.nxtara.devoluciones.api.carga.dto.CargaResponse;
import cl.nxtara.devoluciones.application.CargaService;
import cl.nxtara.devoluciones.application.UsuarioActual;
import cl.nxtara.devoluciones.domain.Rol;
import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/cargas")
public class CargaController {

    private final CargaService cargaService;

    public CargaController(CargaService cargaService) {
        this.cargaService = cargaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CargaResponse> crear(
            @RequestPart("archivo") MultipartFile archivo,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader("X-Rol") String rol
    ) {
        CargaResponse response = cargaService.procesar(archivo, usuarioActual(usuario, rol));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public CargaResponse obtener(@PathVariable Long id) {
        return cargaService.obtener(id);
    }

    private UsuarioActual usuarioActual(String usuario, String rolRaw) {
        try {
            return new UsuarioActual(usuario.trim(), Rol.valueOf(rolRaw.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("Rol inválido: " + rolRaw + " (use ANALISTA o SUPERVISOR)");
        }
    }
}
