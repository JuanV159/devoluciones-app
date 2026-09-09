package cl.nxtara.devoluciones.api.carga;

import cl.nxtara.devoluciones.infrastructure.persistence.CargaErrorRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.CargaRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.UsuarioRepository;
import cl.nxtara.devoluciones.support.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API carga masiva CSV")
class CargaApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private EventoSolicitudRepository eventoSolicitudRepository;

    @Autowired
    private CargaErrorRepository cargaErrorRepository;

    @Autowired
    private CargaRepository cargaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAnalista;

    @BeforeEach
    void setUp() throws Exception {
        cargaErrorRepository.deleteAll();
        eventoSolicitudRepository.deleteAll();
        solicitudRepository.deleteAll();
        cargaRepository.deleteAll();
        TestAuthHelper.ensureUsers(usuarioRepository, passwordEncoder);
        tokenAnalista = TestAuthHelper.bearerToken(mockMvc, objectMapper, "analista1");
    }

    @Test
    void procesaCsvEjemploConToleranciaAErrores() throws Exception {
        MockMultipartFile archivo = csvEjemplo();

        MvcResult result = mockMvc.perform(multipart("/api/v1/cargas")
                        .file(archivo)
                        .header(HttpHeaders.AUTHORIZATION, tokenAnalista))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.totalFilas").value(1000))
                .andExpect(jsonPath("$.filasOk").value(950))
                .andExpect(jsonPath("$.filasRechazadas").value(50))
                .andExpect(jsonPath("$.errores.length()").value(50))
                .andReturn();

        assertEquals(950, solicitudRepository.count());

        Long cargaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/cargas/{id}", cargaId)
                        .header(HttpHeaders.AUTHORIZATION, tokenAnalista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filasOk").value(950));
    }

    @Test
    void reenvioEsIdempotentePorReferenciaBanco() throws Exception {
        mockMvc.perform(multipart("/api/v1/cargas")
                        .file(csvEjemplo())
                        .header(HttpHeaders.AUTHORIZATION, tokenAnalista))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filasOk").value(950));

        mockMvc.perform(multipart("/api/v1/cargas")
                        .file(csvEjemplo())
                        .header(HttpHeaders.AUTHORIZATION, tokenAnalista))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filasOk").value(0))
                .andExpect(jsonPath("$.filasOmitidas").value(950))
                .andExpect(jsonPath("$.filasRechazadas").value(50));

        assertEquals(950, solicitudRepository.count());
    }

    private MockMultipartFile csvEjemplo() throws Exception {
        byte[] bytes = Files.readAllBytes(new ClassPathResource("pagos_banco_ejemplo.csv").getFile().toPath());
        return new MockMultipartFile(
                "archivo",
                "pagos_banco_ejemplo.csv",
                MediaType.TEXT_PLAIN_VALUE,
                bytes
        );
    }
}
