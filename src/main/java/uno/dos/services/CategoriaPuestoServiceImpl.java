package uno.dos.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.CategoriaPuesto;
import uno.dos.repositories.CategoriaPuestoRepository;

@Service
@RequiredArgsConstructor
public class CategoriaPuestoServiceImpl implements CategoriaPuestoService {

    private final CategoriaPuestoRepository repo;

    @Override
    public List<CategoriaPuesto> listar() {
        return repo.findByActivoTrue();
    }

    @Override
    public CategoriaPuesto guardar(CategoriaPuesto categoria) {
        return repo.save(categoria);
    }
}