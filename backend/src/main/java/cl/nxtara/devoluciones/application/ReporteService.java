package cl.nxtara.devoluciones.application;

import cl.nxtara.devoluciones.api.reporte.dto.BancoTopResponse;
import cl.nxtara.devoluciones.api.reporte.dto.ConciliacionDiaResponse;
import cl.nxtara.devoluciones.api.reporte.dto.ConciliacionResponse;
import cl.nxtara.devoluciones.domain.exception.ReglaNegocioException;
import cl.nxtara.devoluciones.infrastructure.persistence.ConciliacionQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ReporteService {

    private static final int TOP_BANCOS = 5;

    private final ConciliacionQueryRepository conciliacionQueryRepository;

    public ReporteService(ConciliacionQueryRepository conciliacionQueryRepository) {
        this.conciliacionQueryRepository = conciliacionQueryRepository;
    }

    @Transactional(readOnly = true)
    public ConciliacionResponse conciliacion(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Los parámetros desde y hasta son obligatorios (ISO date)");
        }
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("hasta no puede ser anterior a desde");
        }

        Instant desdeInclusive = desde.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant hastaExclusive = hasta.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<ConciliacionDiaResponse> porDia = conciliacionQueryRepository.agregarPorDia(desdeInclusive, hastaExclusive);
        List<BancoTopResponse> topBancos = conciliacionQueryRepository.topBancos(desdeInclusive, hastaExclusive, TOP_BANCOS);

        return new ConciliacionResponse(desde, hasta, porDia, topBancos);
    }
}
