package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Material;

public interface MaterialService {

    Material guardar(Material material);

    List<Material> listarPorCurso(Long cursoId);
    
    

}