package cl.nxtara.devoluciones.infrastructure.carga;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CsvCargaParser {

    private static final String SEPARADOR = ";";
    private static final int COLUMNAS = 6;

    public List<FilaCsv> parsear(MultipartFile archivo) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            List<FilaCsv> filas = new ArrayList<>();
            String linea = reader.readLine();
            if (linea == null) {
                return filas;
            }
            // salta encabezado (fila 1)
            int numeroFila = 1;
            while ((linea = reader.readLine()) != null) {
                numeroFila++;
                if (linea.isBlank()) {
                    continue;
                }
                filas.add(parsearLinea(numeroFila, linea));
            }
            return filas;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el archivo CSV", ex);
        }
    }

    private FilaCsv parsearLinea(int numeroFila, String linea) {
        String[] partes = linea.split(SEPARADOR, -1);
        if (partes.length < COLUMNAS) {
            return new FilaCsv(
                    numeroFila,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    true
            );
        }
        return new FilaCsv(
                numeroFila,
                optional(partes[0]),
                optional(partes[1]),
                optional(partes[2]),
                optional(partes[3]),
                optional(partes[4]),
                optional(partes[5]),
                false
        );
    }

    private Optional<String> optional(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        String trim = valor.trim();
        return trim.isEmpty() ? Optional.empty() : Optional.of(trim);
    }
}
