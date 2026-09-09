package cl.nxtara.devoluciones;

import cl.nxtara.devoluciones.config.CargaProperties;
import cl.nxtara.devoluciones.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableConfigurationProperties({CargaProperties.class, JwtProperties.class})
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class DevolucionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevolucionesApplication.class, args);
    }
}
