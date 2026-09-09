package cl.nxtara.devoluciones.api.solicitud;

import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API REST solicitudes y transiciones")
class SolicitudApiTest {

    private static final String BODY_VALIDA = """
            {
              "rutCliente": "6876966-3",
              "nombreCliente": "FELIPE SOTO",
              "monto": 150000.00,
              "bancoDestino": "BANCO ESTADO",
              "cuentaDestino": "123456"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private EventoSolicitudRepository eventoSolicitudRepository;

    @BeforeEach
    void clean() {
        eventoSolicitudRepository.deleteAll();
        solicitudRepository.deleteAll();
    }

    @Test
    void crearDevuelve201YLocation() throws Exception {
        mockMvc.perform(post("/api/v1/solicitudes")
                        .header("X-Usuario", "analista1")
                        .header("X-Rol", "ANALISTA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.folio").value(org.hamcrest.Matchers.startsWith("DEV-")));
    }

    @Test
    void cicloFelizEnviarAprobarPagar() throws Exception {
        long id = crearSolicitud("analista1");

        mockMvc.perform(post("/api/v1/solicitudes/{id}/enviar", id)
                        .header("X-Usuario", "analista1")
                        .header("X-Rol", "ANALISTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));

        mockMvc.perform(post("/api/v1/solicitudes/{id}/aprobar", id)
                        .header("X-Usuario", "supervisor1")
                        .header("X-Rol", "SUPERVISOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        mockMvc.perform(post("/api/v1/solicitudes/{id}/pagar", id)
                        .header("X-Usuario", "supervisor1")
                        .header("X-Rol", "SUPERVISOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));

        mockMvc.perform(get("/api/v1/solicitudes/{id}/historial", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void transicionInvalidaDevuelve409() throws Exception {
        long id = crearSolicitud("analista1");

        mockMvc.perform(post("/api/v1/solicitudes/{id}/pagar", id)
                        .header("X-Usuario", "supervisor1")
                        .header("X-Rol", "SUPERVISOR"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detalle").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/solicitudes/" + id + "/pagar"));
    }

    @Test
    void analistaNoPuedeAprobarDevuelve403() throws Exception {
        long id = crearSolicitud("analista1");
        mockMvc.perform(post("/api/v1/solicitudes/{id}/enviar", id)
                .header("X-Usuario", "analista1")
                .header("X-Rol", "ANALISTA"));

        mockMvc.perform(post("/api/v1/solicitudes/{id}/aprobar", id)
                        .header("X-Usuario", "analista1")
                        .header("X-Rol", "ANALISTA"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void creadorNoPuedeAprobarAunqueSeaSupervisor() throws Exception {
        long id = crearSolicitud("supervisor1");
        mockMvc.perform(post("/api/v1/solicitudes/{id}/enviar", id)
                .header("X-Usuario", "supervisor1")
                .header("X-Rol", "SUPERVISOR"));

        mockMvc.perform(post("/api/v1/solicitudes/{id}/aprobar", id)
                        .header("X-Usuario", "supervisor1")
                        .header("X-Rol", "SUPERVISOR"))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazarSinMotivoDevuelve400() throws Exception {
        long id = crearSolicitud("analista1");
        mockMvc.perform(post("/api/v1/solicitudes/{id}/enviar", id)
                .header("X-Usuario", "analista1")
                .header("X-Rol", "ANALISTA"));

        mockMvc.perform(post("/api/v1/solicitudes/{id}/rechazar", id)
                        .header("X-Usuario", "supervisor1")
                        .header("X-Rol", "SUPERVISOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editarFueraDeBorradorDevuelve409() throws Exception {
        long id = crearSolicitud("analista1");
        mockMvc.perform(post("/api/v1/solicitudes/{id}/enviar", id)
                .header("X-Usuario", "analista1")
                .header("X-Rol", "ANALISTA"));

        mockMvc.perform(put("/api/v1/solicitudes/{id}", id)
                        .header("X-Usuario", "analista1")
                        .header("X-Rol", "ANALISTA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDA))
                .andExpect(status().isConflict());
    }

    @Test
    void rutInvalidoDevuelve400() throws Exception {
        String body = BODY_VALIDA.replace("6876966-3", "19350791-9");
        mockMvc.perform(post("/api/v1/solicitudes")
                        .header("X-Usuario", "analista1")
                        .header("X-Rol", "ANALISTA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarFiltraPorEstadoEnBaseDeDatos() throws Exception {
        crearSolicitud("analista1");
        long id2 = crearSolicitud("analista1");
        mockMvc.perform(post("/api/v1/solicitudes/{id}/enviar", id2)
                .header("X-Usuario", "analista1")
                .header("X-Rol", "ANALISTA"));

        mockMvc.perform(get("/api/v1/solicitudes")
                        .param("estado", "EN_REVISION")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].estado").value("EN_REVISION"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private long crearSolicitud(String usuario) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/solicitudes")
                        .header("X-Usuario", usuario)
                        .header("X-Rol", usuario.startsWith("supervisor") ? "SUPERVISOR" : "ANALISTA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDA))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(json.get("folio").asText().matches("DEV-\\d{4}-\\d{6}"));
        assertEquals(Estado.BORRADOR.name(), json.get("estado").asText());
        return json.get("id").asLong();
    }
}
