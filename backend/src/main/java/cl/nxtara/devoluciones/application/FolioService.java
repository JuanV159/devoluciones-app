package cl.nxtara.devoluciones.application;

import cl.nxtara.devoluciones.infrastructure.persistence.FolioSecuenciaEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.FolioSecuenciaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
public class FolioService {

    private final FolioSecuenciaRepository folioSecuenciaRepository;
    private final EntityManager entityManager;

    public FolioService(FolioSecuenciaRepository folioSecuenciaRepository, EntityManager entityManager) {
        this.folioSecuenciaRepository = folioSecuenciaRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public String siguienteFolio() {
        int anio = Year.now().getValue();
        FolioSecuenciaEntity secuencia = folioSecuenciaRepository.findById(anio)
                .orElseGet(() -> {
                    FolioSecuenciaEntity nueva = new FolioSecuenciaEntity();
                    nueva.setAnio(anio);
                    nueva.setUltimoNumero(0);
                    return folioSecuenciaRepository.saveAndFlush(nueva);
                });

        entityManager.lock(secuencia, LockModeType.PESSIMISTIC_WRITE);
        int siguiente = secuencia.getUltimoNumero() + 1;
        secuencia.setUltimoNumero(siguiente);
        folioSecuenciaRepository.save(secuencia);
        return "DEV-%d-%06d".formatted(anio, siguiente);
    }
}
