package cl.nxtara.devoluciones.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "folio_secuencia")
public class FolioSecuenciaEntity {

    @Id
    @Column(name = "anio")
    private Integer anio;

    @Column(name = "ultimo_numero", nullable = false)
    private int ultimoNumero;

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public int getUltimoNumero() {
        return ultimoNumero;
    }

    public void setUltimoNumero(int ultimoNumero) {
        this.ultimoNumero = ultimoNumero;
    }
}
