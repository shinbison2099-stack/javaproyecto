package uno.dos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uno.dos.models.entity.Material;
import uno.dos.repositories.MaterialRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    @Override
    public Material guardar(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public List<Material> listarPorCurso(Long cursoId) {
        return materialRepository.findByCursoId(cursoId);
    }
    
    
    
}