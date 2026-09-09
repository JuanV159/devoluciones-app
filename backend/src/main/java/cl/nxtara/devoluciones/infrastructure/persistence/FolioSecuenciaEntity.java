package cl.nxtara.devoluciones.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "folio_secuencia")
@Getter
@Setter
@NoArgsConstructor
public class FolioSecuenciaEntity {

    @Id
    @Column(name = "anio")
    private Integer anio;

    @Column(name = "ultimo_numero", nullable = false)
    private int ultimoNumero;
}
