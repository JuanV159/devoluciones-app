package cl.nxtara.devoluciones.application;

import cl.nxtara.devoluciones.api.auth.dto.LoginRequest;
import cl.nxtara.devoluciones.api.auth.dto.LoginResponse;
import cl.nxtara.devoluciones.infrastructure.security.JwtService;
import cl.nxtara.devoluciones.infrastructure.security.UsuarioPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
            String token = jwtService.generarToken(principal.getUsername(), principal.getRol());
            return new LoginResponse(token, jwtService.expiresInSeconds(), principal.getRol());
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }
}
