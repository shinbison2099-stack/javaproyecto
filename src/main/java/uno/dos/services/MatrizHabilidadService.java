package uno.dos.services;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import uno.dos.models.entity.MatrizHabilidad;
import uno.dos.models.entity.TipoTrabajador;

public interface MatrizHabilidadService {

    List<MatrizHabilidad> listar();

    Optional<MatrizHabilidad> buscarPorId(Long id);

   
    MatrizHabilidad guardar(
            String nombre,
            Long areaId,
            Long subAreaId,
            TipoTrabajador tipoTrabajador,
            List<Long> habilidadIds,
            List<Long> cursoIds,
            List<Long> trabajadorIds
    );
    
    void actualizar(
            Long id,
            String nombre,
            Long areaId,
            Long subAreaId,
            TipoTrabajador tipoTrabajador,
            List<Long> habilidadIds,
            List<Long> cursoIds,
            List<Long> trabajadorIds
    );
    
    
    public void eliminar(Long id);

}
