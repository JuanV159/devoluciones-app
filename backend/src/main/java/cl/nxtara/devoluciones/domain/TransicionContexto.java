package cl.nxtara.devoluciones.domain;

/**
 * Datos necesarios para evaluar una transición sin acoplar la máquina a JPA.
 */
public record TransicionContexto(
        String usuario,
        Rol rol,
        String creadaPor,
        int reaperturas,
        String motivoRechazo,
        String comentario
) {
    public TransicionContexto {
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("usuario es obligatorio");
        }
        if (rol == null) {
            throw new IllegalArgumentException("rol es obligatorio");
        }
        if (creadaPor == null || creadaPor.isBlank()) {
            throw new IllegalArgumentException("creadaPor es obligatorio");
        }
        if (reaperturas < 0) {
            throw new IllegalArgumentException("reaperturas no puede ser negativa");
        }
    }

    public static TransicionContexto de(String usuario, Rol rol, String creadaPor, int reaperturas) {
        return new TransicionContexto(usuario, rol, creadaPor, reaperturas, null, null);
    }

    public TransicionContexto conMotivoRechazo(String motivo) {
        return new TransicionContexto(usuario, rol, creadaPor, reaperturas, motivo, comentario);
    }

    public TransicionContexto conComentario(String comentario) {
        return new TransicionContexto(usuario, rol, creadaPor, reaperturas, motivoRechazo, comentario);
    }
}
