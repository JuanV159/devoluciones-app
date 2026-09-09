package cl.nxtara.devoluciones.application;

import cl.nxtara.devoluciones.domain.exception.AccesoDenegadoException;
import cl.nxtara.devoluciones.infrastructure.security.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioActualProvider {

    public UsuarioActual require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new AccesoDenegadoException("No hay un usuario autenticado");
        }
        return new UsuarioActual(principal.getUsername(), principal.getRol());
    }
}
