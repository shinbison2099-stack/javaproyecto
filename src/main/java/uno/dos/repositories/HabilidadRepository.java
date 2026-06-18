package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.Habilidad;
import uno.dos.models.entity.SubArea;

public interface HabilidadRepository
        extends JpaRepository<Habilidad, Long>{

    List<Habilidad> findByActivoTrue();

    List<Habilidad> findBySubAreaAndActivoTrue(
            SubArea subArea
    );

    boolean existsByNombreHabilidadAndSubArea(
            String nombreHabilidad,
            SubArea subArea
    );
    
    List<Habilidad> findBySubAreaId(Long subAreaId);
}