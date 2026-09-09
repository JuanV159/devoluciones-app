package cl.nxtara.devoluciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoSolicitudRepository extends JpaRepository<EventoSolicitudEntity, Long> {

    List<EventoSolicitudEntity> findBySolicitudIdOrderByFechaAsc(Long solicitudId);
}
