package cl.nxtara.devoluciones.api.carga;

import cl.nxtara.devoluciones.api.carga.dto.CargaResponse;
import cl.nxtara.devoluciones.application.CargaService;
import cl.nxtara.devoluciones.application.UsuarioActualProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/cargas")
public class CargaController {

    private final CargaService cargaService;
    private final UsuarioActualProvider usuarioActualProvider;

    public CargaController(CargaService cargaService, UsuarioActualProvider usuarioActualProvider) {
        this.cargaService = cargaService;
        this.usuarioActualProvider = usuarioActualProvider;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CargaResponse> crear(@RequestPart("archivo") MultipartFile archivo) {
        CargaResponse response = cargaService.procesar(archivo, usuarioActualProvider.require());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public CargaResponse obtener(@PathVariable Long id) {
        return cargaService.obtener(id);
    }
}
