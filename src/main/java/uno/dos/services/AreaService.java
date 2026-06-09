package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.Area;

public interface AreaService {

    List<Area> listar();

    List<Area> listarEliminadas();

    Optional<Area> buscarPorId(Long id);

    Area guardar(Area area);

    void eliminar(Long id);

    boolean existeNombre(
            String nombreArea
    );
}