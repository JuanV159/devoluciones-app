package cl.nxtara.devoluciones.domain;

import cl.nxtara.devoluciones.domain.exception.AccesoDenegadoException;
import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;
import cl.nxtara.devoluciones.domain.exception.TransicionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MaquinaEstados — reglas R1–R7")
class MaquinaEstadosTest {

    private static TransicionContexto analista(String usuario, String creadaPor, int reaperturas) {
        return TransicionContexto.de(usuario, Rol.ANALISTA, creadaPor, reaperturas);
    }

    private static TransicionContexto supervisor(String usuario, String creadaPor, int reaperturas) {
        return TransicionContexto.de(usuario, Rol.SUPERVISOR, creadaPor, reaperturas);
    }

    @Nested
    @DisplayName("R1 — transiciones válidas e inválidas")
    class ReglaR1 {

        @Test
        void enviarDesdeBorradorLlevaAEnRevision() {
            var resultado = MaquinaEstados.transicionar(
                    Estado.BORRADOR,
                    Accion.ENVIAR,
                    analista("analista1", "analista1", 0)
            );
            assertEquals(Estado.EN_REVISION, resultado.estadoDestino());
        }

        @Test
        void anularDesdeBorradorLlevaAAnulada() {
            var resultado = MaquinaEstados.transicionar(
                    Estado.BORRADOR,
                    Accion.ANULAR,
                    analista("analista1", "analista1", 0)
            );
            assertEquals(Estado.ANULADA, resultado.estadoDestino());
        }

        @Test
        void aprobarDesdeEnRevisionLlevaAAprobada() {
            var resultado = MaquinaEstados.transicionar(
                    Estado.EN_REVISION,
                    Accion.APROBAR,
                    supervisor("supervisor1", "analista1", 0)
            );
            assertEquals(Estado.APROBADA, resultado.estadoDestino());
        }

        @Test
        void rechazarDesdeEnRevisionLlevaARechazada() {
            var contexto = supervisor("supervisor1", "analista1", 0)
                    .conMotivoRechazo("Documentación incompleta");
            var resultado = MaquinaEstados.transicionar(Estado.EN_REVISION, Accion.RECHAZAR, contexto);
            assertEquals(Estado.RECHAZADA, resultado.estadoDestino());
            assertEquals("Documentación incompleta", resultado.motivoRechazo());
        }

        @Test
        void pagarDesdeAprobadaLlevaAPagada() {
            var resultado = MaquinaEstados.transicionar(
                    Estado.APROBADA,
                    Accion.PAGAR,
                    supervisor("supervisor1", "analista1", 0)
            );
            assertEquals(Estado.PAGADA, resultado.estadoDestino());
        }

        @Test
        void reabrirDesdeRechazadaLlevaABorrador() {
            var resultado = MaquinaEstados.transicionar(
                    Estado.RECHAZADA,
                    Accion.REABRIR,
                    analista("analista1", "analista1", 0)
            );
            assertEquals(Estado.BORRADOR, resultado.estadoDestino());
            assertEquals(1, resultado.reaperturas());
        }

        @ParameterizedTest(name = "{0} no puede {1}")
        @CsvSource({
                "BORRADOR,APROBAR",
                "BORRADOR,RECHAZAR",
                "BORRADOR,PAGAR",
                "BORRADOR,REABRIR",
                "EN_REVISION,ENVIAR",
                "EN_REVISION,PAGAR",
                "EN_REVISION,ANULAR",
                "EN_REVISION,REABRIR",
                "APROBADA,ENVIAR",
                "APROBADA,APROBAR",
                "APROBADA,RECHAZAR",
                "APROBADA,ANULAR",
                "RECHAZADA,APROBAR",
                "RECHAZADA,PAGAR",
                "PAGADA,ENVIAR",
                "PAGADA,APROBAR",
                "PAGADA,RECHAZAR",
                "PAGADA,PAGAR",
                "PAGADA,REABRIR",
                "PAGADA,ANULAR",
                "ANULADA,ENVIAR",
                "ANULADA,APROBAR",
                "ANULADA,REABRIR"
        })
        void transicionInvalidaLanzaConflicto(Estado estado, Accion accion) {
            TransicionContexto contexto = ACCIONES_SUPERVISOR.contains(accion)
                    ? supervisor("supervisor1", "analista1", 0).conMotivoRechazo("x")
                    : analista("analista1", "analista1", 0);

            assertThrows(
                    TransicionInvalidaException.class,
                    () -> MaquinaEstados.transicionar(estado, accion, contexto)
            );
        }

        private static final java.util.Set<Accion> ACCIONES_SUPERVISOR = java.util.Set.of(
                Accion.APROBAR, Accion.RECHAZAR, Accion.PAGAR
        );
    }

    @Nested
    @DisplayName("R2 — roles")
    class ReglaR2 {

        @ParameterizedTest
        @EnumSource(value = Accion.class, names = {"APROBAR", "RECHAZAR", "PAGAR"})
        void accionesDeSupervisorDenieganAnalista(Accion accion) {
            var contexto = analista("analista1", "otro", 0).conMotivoRechazo("motivo");
            assertThrows(
                    AccesoDenegadoException.class,
                    () -> MaquinaEstados.transicionar(estadoRequerido(accion), accion, contexto)
            );
        }

        @Test
        void analistaPuedeEnviarAnularYReabrir() {
            assertEquals(
                    Estado.EN_REVISION,
                    MaquinaEstados.transicionar(Estado.BORRADOR, Accion.ENVIAR, analista("a1", "a1", 0)).estadoDestino()
            );
            assertEquals(
                    Estado.ANULADA,
                    MaquinaEstados.transicionar(Estado.BORRADOR, Accion.ANULAR, analista("a1", "a1", 0)).estadoDestino()
            );
            assertEquals(
                    Estado.BORRADOR,
                    MaquinaEstados.transicionar(Estado.RECHAZADA, Accion.REABRIR, analista("a1", "a1", 0)).estadoDestino()
            );
        }

        private Estado estadoRequerido(Accion accion) {
            return switch (accion) {
                case APROBAR, RECHAZAR -> Estado.EN_REVISION;
                case PAGAR -> Estado.APROBADA;
                default -> throw new IllegalStateException();
            };
        }
    }

    @Nested
    @DisplayName("R3 — motivo de rechazo")
    class ReglaR3 {

        @Test
        void rechazarSinMotivoFalla() {
            var contexto = supervisor("supervisor1", "analista1", 0);
            assertThrows(
                    ReglaNegocioException.class,
                    () -> MaquinaEstados.transicionar(Estado.EN_REVISION, Accion.RECHAZAR, contexto)
            );
        }

        @Test
        void rechazarConMotivoEnBlancoFalla() {
            var contexto = supervisor("supervisor1", "analista1", 0).conMotivoRechazo("   ");
            assertThrows(
                    ReglaNegocioException.class,
                    () -> MaquinaEstados.transicionar(Estado.EN_REVISION, Accion.RECHAZAR, contexto)
            );
        }
    }

    @Nested
    @DisplayName("R4 — reapertura máxima 1 vez")
    class ReglaR4 {

        @Test
        void primeraReaperturaOk() {
            var resultado = MaquinaEstados.transicionar(
                    Estado.RECHAZADA,
                    Accion.REABRIR,
                    analista("analista1", "analista1", 0)
            );
            assertEquals(1, resultado.reaperturas());
        }

        @Test
        void segundaReaperturaFalla() {
            assertThrows(
                    TransicionInvalidaException.class,
                    () -> MaquinaEstados.transicionar(
                            Estado.RECHAZADA,
                            Accion.REABRIR,
                            analista("analista1", "analista1", 1)
                    )
            );
        }
    }

    @Nested
    @DisplayName("R7 — separación de funciones")
    class ReglaR7 {

        @Test
        void creadorNoPuedeAprobarAunqueSeaSupervisor() {
            var contexto = supervisor("juan", "juan", 0);
            assertThrows(
                    TransicionInvalidaException.class,
                    () -> MaquinaEstados.transicionar(Estado.EN_REVISION, Accion.APROBAR, contexto)
            );
        }

        @Test
        void otroSupervisorPuedeAprobar() {
            var resultado = MaquinaEstados.transicionar(
                    Estado.EN_REVISION,
                    Accion.APROBAR,
                    supervisor("supervisor1", "analista1", 0)
            );
            assertEquals(Estado.APROBADA, resultado.estadoDestino());
            assertNull(resultado.motivoRechazo());
        }
    }
}
