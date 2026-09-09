package cl.nxtara.devoluciones.support;

import cl.nxtara.devoluciones.domain.Rol;
import cl.nxtara.devoluciones.infrastructure.persistence.UsuarioEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestAuthHelper {

    public static final String PASSWORD = "Password123!";

    private TestAuthHelper() {
    }

    public static void ensureUsers(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        ensure(usuarioRepository, passwordEncoder, "analista1", Rol.ANALISTA);
        ensure(usuarioRepository, passwordEncoder, "supervisor1", Rol.SUPERVISOR);
    }

    public static String bearerToken(MockMvc mockMvc, ObjectMapper objectMapper, String username) throws Exception {
        String body = """
                {"username":"%s","password":"%s"}
                """.formatted(username, PASSWORD);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return "Bearer " + json.get("access_token").asText();
    }

    private static void ensure(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            String username,
            Rol rol
    ) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }
        UsuarioEntity user = new UsuarioEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRol(rol);
        user.setActivo(true);
        usuarioRepository.save(user);
    }
}
