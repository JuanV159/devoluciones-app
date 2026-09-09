package cl.nxtara.devoluciones.api.reporte;

import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.domain.Origen;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudEntity;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API reporte de conciliación")
class ConciliacionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private EventoSolicitudRepository eventoSolicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        eventoSolicitudRepository.deleteAll();
        solicitudRepository.deleteAll();
        TestAuthHelper.ensureUsers(usuarioRepository, passwordEncoder);
        token = TestAuthHelper.bearerToken(mockMvc, objectMapper, "analista1");

        Instant dia = LocalDate.of(2026, 7, 10).atTime(12, 0).toInstant(ZoneOffset.UTC);
        guardar("DEV-2026-R001", "BANCO ESTADO", "100000", Estado.BORRADOR, dia);
        guardar("DEV-2026-R002", "BANCO ESTADO", "200000", Estado.APROBADA, dia);
        guardar("DEV-2026-R003", "ITAU", "300000", Estado.PAGADA, dia);
        guardar("DEV-2026-R004", "ITAU", "400000", Estado.RECHAZADA, dia, "motivo");
    }

    @Test
    void conciliacionAgregaPorDiaYTopBancosEnSql() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/conciliacion")
                        .param("desde", "2026-07-01")
                        .param("hasta", "2026-07-21")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").value("2026-07-01"))
                .andExpect(jsonPath("$.hasta").value("2026-07-21"))
                .andExpect(jsonPath("$.porDia", hasSize(1)))
                .andExpect(jsonPath("$.porDia[0].fecha").value("2026-07-10"))
                .andExpect(jsonPath("$.porDia[0].totalSolicitado").value(1000000.0))
                .andExpect(jsonPath("$.porDia[0].totalAprobado").value(500000.0))
                .andExpect(jsonPath("$.porDia[0].totalPagado").value(300000.0))
                .andExpect(jsonPath("$.porDia[0].tasaRechazo").value(0.25))
                .andExpect(jsonPath("$.topBancos", hasSize(2)))
                .andExpect(jsonPath("$.topBancos[0].bancoDestino").value("ITAU"))
                .andExpect(jsonPath("$.topBancos[0].montoTotal").value(700000.0));
    }

    @Test
    void conciliacionSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/conciliacion")
                        .param("desde", "2026-07-01")
                        .param("hasta", "2026-07-21"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conciliacionConRangoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/conciliacion")
                        .param("desde", "2026-07-21")
                        .param("hasta", "2026-07-01")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
    }

    private void guardar(
            String folio,
            String banco,
            String monto,
            Estado estado,
            Instant fecha
    ) {
        guardar(folio, banco, monto, estado, fecha, null);
    }

    private void guardar(
            String folio,
            String banco,
            String monto,
            Estado estado,
            Instant fecha,
            String motivo
    ) {
        SolicitudEntity s = new SolicitudEntity();
        s.setFolio(folio);
        s.setRutCliente("6876966-3");
        s.setNombreCliente("TEST");
        s.setMonto(new BigDecimal(monto));
        s.setMoneda("CLP");
        s.setBancoDestino(banco);
        s.setCuentaDestino("1");
        s.setOrigen(Origen.MANUAL);
        s.setEstado(estado);
        s.setMotivoRechazo(motivo);
        s.setReaperturas(0);
        s.setCreadaPor("analista1");
        s.setFechaCreacion(fecha);
        s.setActualizadaPor("analista1");
        s.setFechaActualizacion(fecha);
        solicitudRepository.save(s);
    }
}
