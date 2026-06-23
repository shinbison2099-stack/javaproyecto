package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.Habilidad;
import uno.dos.models.entity.SubArea;

public interface HabilidadService {

    List<Habilidad> listar();

    List<Habilidad> listarPorSubArea(
            SubArea subArea
    );

    Optional<Habilidad> buscarPorId(
            Long id
    );

    Habilidad guardar(
            Habilidad habilidad
    );

    void eliminar(
            Long id
    );

    boolean existeNombre(
            String nombreHabilidad,
            SubArea subArea
    );
    
    List<Habilidad> buscarPorSubArea(
            Long subAreaId
    );
    
    Habilidad guardarDesdeWizard(
            Long subAreaId,
            String nombreHabilidad
    );
}