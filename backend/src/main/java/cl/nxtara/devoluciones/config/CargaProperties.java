package cl.nxtara.devoluciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.carga")
public class CargaProperties {

    private String dir = "./cargas";
    private int maxFilas = 50_000;
    private int chunkSize = 200;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public int getMaxFilas() {
        return maxFilas;
    }

    public void setMaxFilas(int maxFilas) {
        this.maxFilas = maxFilas;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
}
