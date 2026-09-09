package cl.nxtara.devoluciones.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carga_error")
@Getter
@Setter
@NoArgsConstructor
public class CargaErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carga_id", nullable = false)
    private CargaEntity carga;

    @Column(nullable = false)
    private int fila;

    @Column(length = 50)
    private String campo;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "referencia_banco", length = 100)
    private String referenciaBanco;
}
