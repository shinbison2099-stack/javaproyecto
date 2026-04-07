package uno.dos.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    @Value("${ruta.fotos}")
    private String rutaFotos;

    @PostConstruct
    public void crearCarpeta() {

        try {

            Path ruta = Paths.get(rutaFotos);

            if (!Files.exists(ruta)) {
                Files.createDirectories(ruta);
                System.out.println("Carpeta creada: " + rutaFotos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}