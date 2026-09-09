package cl.nxtara.devoluciones.application;

import cl.nxtara.devoluciones.api.carga.dto.CargaErrorResponse;
import cl.nxtara.devoluciones.api.carga.dto.CargaResponse;
import cl.nxtara.devoluciones.config.CargaProperties;
import cl.nxtara.devoluciones.domain.Estado;
import cl.nxtara.devoluciones.domain.EstadoCarga;
import cl.nxtara.devoluciones.domain.Origen;
import cl.nxtara.devoluciones.domain.exception.RecursoNoEncontradoException;
import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;
import cl.nxtara.devoluciones.domain.validation.MontoValidator;
import cl.nxtara.devoluciones.domain.validation.RutChilenoValidator;
import cl.nxtara.devoluciones.infrastructure.carga.CsvCargaParser;
import cl.nxtara.devoluciones.infrastructure.carga.FilaCsv;
import cl.nxtara.devoluciones.infrastructure.persistence.CargaEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.CargaErrorEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.CargaErrorRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.CargaRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.EventoSolicitudRepository;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.SolicitudRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class CargaService {

    private final CargaRepository cargaRepository;
    private final CargaErrorRepository cargaErrorRepository;
    private final SolicitudRepository solicitudRepository;
    private final EventoSolicitudRepository eventoSolicitudRepository;
    private final FolioService folioService;
    private final CsvCargaParser csvCargaParser;
    private final CargaProperties cargaProperties;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public CargaService(
            CargaRepository cargaRepository,
            CargaErrorRepository cargaErrorRepository,
            SolicitudRepository solicitudRepository,
            EventoSolicitudRepository eventoSolicitudRepository,
            FolioService folioService,
            CsvCargaParser csvCargaParser,
            CargaProperties cargaProperties,
            EntityManager entityManager,
            PlatformTransactionManager transactionManager
    ) {
        this.cargaRepository = cargaRepository;
        this.cargaErrorRepository = cargaErrorRepository;
        this.solicitudRepository = solicitudRepository;
        this.eventoSolicitudRepository = eventoSolicitudRepository;
        this.folioService = folioService;
        this.csvCargaParser = csvCargaParser;
        this.cargaProperties = cargaProperties;
        this.entityManager = entityManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public CargaResponse procesar(MultipartFile archivo, UsuarioActual usuario) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("El archivo CSV es obligatorio");
        }

        List<FilaCsv> filas = csvCargaParser.parsear(archivo);
        if (filas.size() > cargaProperties.getMaxFilas()) {
            throw new ReglaNegocioException(
                    "El archivo supera el máximo de " + cargaProperties.getMaxFilas() + " filas"
            );
        }

        Long cargaId = transactionTemplate.execute(status -> {
            CargaEntity carga = new CargaEntity();
            carga.setNombreArchivo(Optional.ofNullable(archivo.getOriginalFilename()).orElse("sin-nombre.csv"));
            carga.setEstado(EstadoCarga.PROCESANDO);
            carga.setTotalFilas(filas.size());
            carga.setFilasOk(0);
            carga.setFilasRechazadas(0);
            carga.setFilasOmitidas(0);
            carga.setCreadaPor(usuario.username());
            carga.setFechaCreacion(Instant.now());
            return cargaRepository.save(carga).getId();
        });

        Contadores contadores = new Contadores();
        Set<String> referenciasVistas = new HashSet<>();
        int chunkSize = Math.max(1, cargaProperties.getChunkSize());

        try {
            for (int i = 0; i < filas.size(); i += chunkSize) {
                List<FilaCsv> chunk = filas.subList(i, Math.min(i + chunkSize, filas.size()));
                transactionTemplate.executeWithoutResult(status ->
                        procesarChunk(cargaId, chunk, usuario.username(), referenciasVistas, contadores)
                );
            }
            transactionTemplate.executeWithoutResult(status -> finalizar(cargaId, EstadoCarga.COMPLETADA, contadores));
        } catch (RuntimeException ex) {
            transactionTemplate.executeWithoutResult(status -> finalizar(cargaId, EstadoCarga.FALLIDA, contadores));
            throw ex;
        }

        return obtener(cargaId);
    }

    public CargaResponse obtener(Long id) {
        CargaEntity carga = cargaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carga no encontrada: " + id));
        List<CargaErrorResponse> errores = cargaErrorRepository.findByCargaIdOrderByFilaAsc(id).stream()
                .map(e -> new CargaErrorResponse(e.getFila(), e.getCampo(), e.getMotivo(), e.getReferenciaBanco()))
                .toList();
        return new CargaResponse(
                carga.getId(),
                carga.getNombreArchivo(),
                carga.getEstado(),
                carga.getTotalFilas(),
                carga.getFilasOk(),
                carga.getFilasRechazadas(),
                carga.getFilasOmitidas(),
                carga.getCreadaPor(),
                carga.getFechaCreacion(),
                carga.getFechaFin(),
                errores
        );
    }

    private void procesarChunk(
            Long cargaId,
            List<FilaCsv> chunk,
            String usuario,
            Set<String> referenciasVistas,
            Contadores contadores
    ) {
        CargaEntity carga = cargaRepository.findById(cargaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carga no encontrada: " + cargaId));

        List<FilaCsv> candidatas = new ArrayList<>();
        for (FilaCsv fila : chunk) {
            Optional<ErrorFila> error = validarFila(fila, referenciasVistas);
            if (error.isPresent()) {
                ErrorFila e = error.get();
                guardarError(carga, fila.numeroFila(), e.campo(), e.motivo(), e.referencia());
                contadores.rechazadas++;
                continue;
            }
            String ref = fila.referenciaBanco().orElseThrow().trim();
            if (solicitudRepository.findByReferenciaBanco(ref).isPresent()) {
                referenciasVistas.add(ref.toUpperCase(Locale.ROOT));
                contadores.omitidas++;
                continue;
            }
            referenciasVistas.add(ref.toUpperCase(Locale.ROOT));
            candidatas.add(fila);
        }

        List<String> folios = folioService.reservarFolios(candidatas.size());
        Instant ahora = Instant.now();
        for (int i = 0; i < candidatas.size(); i++) {
            FilaCsv fila = candidatas.get(i);
            SolicitudEntity solicitud = new SolicitudEntity();
            solicitud.setFolio(folios.get(i));
            solicitud.setRutCliente(fila.rutCliente().orElseThrow().trim().toUpperCase(Locale.ROOT));
            solicitud.setNombreCliente(fila.nombreCliente().orElseThrow().trim());
            solicitud.setMonto(MontoValidator.normalizarYValidar(new BigDecimal(fila.monto().orElseThrow().trim())));
            solicitud.setMoneda("CLP");
            solicitud.setBancoDestino(fila.bancoDestino().orElseThrow().trim());
            solicitud.setCuentaDestino(fila.cuentaDestino().orElseThrow().trim());
            solicitud.setReferenciaBanco(fila.referenciaBanco().orElseThrow().trim());
            solicitud.setOrigen(Origen.CARGA_MASIVA);
            solicitud.setEstado(Estado.EN_REVISION);
            solicitud.setReaperturas(0);
            solicitud.setCreadaPor(usuario);
            solicitud.setFechaCreacion(ahora);
            solicitud.setActualizadaPor(usuario);
            solicitud.setFechaActualizacion(ahora);
            solicitudRepository.save(solicitud);

            EventoSolicitudEntity evento = new EventoSolicitudEntity();
            evento.setSolicitud(solicitud);
            evento.setEstadoOrigen(null);
            evento.setEstadoDestino(Estado.EN_REVISION);
            evento.setUsuario(usuario);
            evento.setFecha(ahora);
            evento.setComentario("Alta por carga masiva");
            eventoSolicitudRepository.save(evento);
            contadores.ok++;
        }

        entityManager.flush();
        entityManager.clear();
    }

    private Optional<ErrorFila> validarFila(FilaCsv fila, Set<String> referenciasVistas) {
        if (fila.incompleta()) {
            return Optional.of(new ErrorFila("fila", "Fila incompleta: se esperaban 6 columnas", null));
        }
        if (fila.rutCliente().isEmpty()) {
            return Optional.of(new ErrorFila("rut_cliente", "Campo vacío", null));
        }
        if (!RutChilenoValidator.esValido(fila.rutCliente().get())) {
            return Optional.of(new ErrorFila("rut_cliente", "RUT inválido", fila.referenciaBanco().orElse(null)));
        }
        if (fila.nombreCliente().isEmpty()) {
            return Optional.of(new ErrorFila("nombre_cliente", "Campo vacío", fila.referenciaBanco().orElse(null)));
        }
        if (fila.monto().isEmpty()) {
            return Optional.of(new ErrorFila("monto", "Campo vacío", fila.referenciaBanco().orElse(null)));
        }
        try {
            MontoValidator.normalizarYValidar(new BigDecimal(fila.monto().get().trim()));
        } catch (NumberFormatException | ArithmeticException ex) {
            return Optional.of(new ErrorFila("monto", "Monto no numérico", fila.referenciaBanco().orElse(null)));
        } catch (ReglaNegocioException ex) {
            return Optional.of(new ErrorFila("monto", ex.getMessage(), fila.referenciaBanco().orElse(null)));
        }
        if (fila.bancoDestino().isEmpty()) {
            return Optional.of(new ErrorFila("banco_destino", "Campo vacío", fila.referenciaBanco().orElse(null)));
        }
        if (fila.cuentaDestino().isEmpty()) {
            return Optional.of(new ErrorFila("cuenta_destino", "Campo vacío", fila.referenciaBanco().orElse(null)));
        }
        if (fila.referenciaBanco().isEmpty()) {
            return Optional.of(new ErrorFila("referencia_banco", "Campo vacío", null));
        }
        String refKey = fila.referenciaBanco().get().trim().toUpperCase(Locale.ROOT);
        if (referenciasVistas.contains(refKey)) {
            return Optional.of(new ErrorFila(
                    "referencia_banco",
                    "Referencia duplicada en el archivo",
                    fila.referenciaBanco().get()
            ));
        }
        return Optional.empty();
    }

    private void guardarError(CargaEntity carga, int fila, String campo, String motivo, String referencia) {
        CargaErrorEntity error = new CargaErrorEntity();
        error.setCarga(carga);
        error.setFila(fila);
        error.setCampo(campo);
        error.setMotivo(motivo);
        error.setReferenciaBanco(referencia);
        cargaErrorRepository.save(error);
    }

    private void finalizar(Long cargaId, EstadoCarga estado, Contadores contadores) {
        CargaEntity carga = cargaRepository.findById(cargaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carga no encontrada: " + cargaId));
        carga.setEstado(estado);
        carga.setFilasOk(contadores.ok);
        carga.setFilasRechazadas(contadores.rechazadas);
        carga.setFilasOmitidas(contadores.omitidas);
        carga.setFechaFin(Instant.now());
        cargaRepository.save(carga);
    }

    private record ErrorFila(String campo, String motivo, String referencia) {
    }

    private static final class Contadores {
        private int ok;
        private int rechazadas;
        private int omitidas;
    }
}
