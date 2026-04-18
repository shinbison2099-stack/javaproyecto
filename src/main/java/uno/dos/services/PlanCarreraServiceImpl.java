package uno.dos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uno.dos.models.entity.PlanCarrera;
import uno.dos.repositories.PlanCarreraRepository;
import uno.dos.services.PlanCarreraService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanCarreraServiceImpl implements PlanCarreraService {

    private final PlanCarreraRepository repository;

    @Override
    public PlanCarrera guardar(PlanCarrera plan) {
        return repository.save(plan);
    }

    @Override
    public List<PlanCarrera> listar() {
        return repository.findAll();
    }

    @Override
    public Optional<PlanCarrera> buscarPorId(Long id) {
        return repository.findById(id);
    }
}
