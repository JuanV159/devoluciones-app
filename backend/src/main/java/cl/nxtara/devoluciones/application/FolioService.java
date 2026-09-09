package cl.nxtara.devoluciones.application;

import cl.nxtara.devoluciones.infrastructure.persistence.FolioSecuenciaEntity;
import cl.nxtara.devoluciones.infrastructure.persistence.FolioSecuenciaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

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
        return reservarFolios(1).getFirst();
    }

    @Transactional
    public List<String> reservarFolios(int cantidad) {
        if (cantidad <= 0) {
            return List.of();
        }
        int anio = Year.now().getValue();
        FolioSecuenciaEntity secuencia = folioSecuenciaRepository.findById(anio)
                .orElseGet(() -> {
                    FolioSecuenciaEntity nueva = new FolioSecuenciaEntity();
                    nueva.setAnio(anio);
                    nueva.setUltimoNumero(0);
                    return folioSecuenciaRepository.saveAndFlush(nueva);
                });

        entityManager.lock(secuencia, LockModeType.PESSIMISTIC_WRITE);
        int desde = secuencia.getUltimoNumero() + 1;
        int hasta = secuencia.getUltimoNumero() + cantidad;
        secuencia.setUltimoNumero(hasta);
        folioSecuenciaRepository.save(secuencia);

        List<String> folios = new ArrayList<>(cantidad);
        for (int n = desde; n <= hasta; n++) {
            folios.add("DEV-%d-%06d".formatted(anio, n));
        }
        return folios;
    }
}
