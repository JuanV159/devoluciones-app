package cl.nxtara.devoluciones.infrastructure.persistence;

import cl.nxtara.devoluciones.domain.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<SolicitudEntity, Long>, JpaSpecificationExecutor<SolicitudEntity> {

    Optional<SolicitudEntity> findByFolio(String folio);

    Optional<SolicitudEntity> findByReferenciaBanco(String referenciaBanco);

    long countByEstado(Estado estado);
}
