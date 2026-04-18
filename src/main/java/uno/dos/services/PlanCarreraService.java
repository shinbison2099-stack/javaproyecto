package uno.dos.services;

import uno.dos.models.entity.PlanCarrera;

import java.util.List;
import java.util.Optional;

public interface PlanCarreraService {

    PlanCarrera guardar(PlanCarrera plan);

    List<PlanCarrera> listar();

    Optional<PlanCarrera> buscarPorId(Long id);
}