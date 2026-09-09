package cl.nxtara.devoluciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CargaErrorRepository extends JpaRepository<CargaErrorEntity, Long> {

    List<CargaErrorEntity> findByCargaIdOrderByFilaAsc(Long cargaId);
}
