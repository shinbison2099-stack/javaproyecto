package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.Area;
import uno.dos.models.entity.SubArea;

public interface SubAreaService {

    List<SubArea> listar();

    List<SubArea> listarPorArea(
            Area area
    );

    Optional<SubArea> buscarPorId(
            Long id
    );

    SubArea guardar(
            SubArea subArea
    );

    void eliminar(Long id);

    boolean existeNombre(
            String nombreSubArea,
            Area area
    );
    
    
}
