package uno.dos.services;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.repositories.CapacitacionTrabajadorRepository;

@Service
@RequiredArgsConstructor
public class CapacitacionTrabajadorServiceImpl implements CapacitacionTrabajadorService {

    private final CapacitacionTrabajadorRepository repository;

    @Override
    public List<CapacitacionTrabajador> listarTodas() {
        return repository.findAll();
    }

    @Override
    public List<CapacitacionTrabajador> listarActivas() {
        return repository.findByActivoTrue();
    }

    @Override
    public Optional<CapacitacionTrabajador> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Override
    public CapacitacionTrabajador guardar(CapacitacionTrabajador ct) {
        return repository.save(ct);
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<CapacitacionTrabajador> buscarPorTrabajador(Long trabajadorId) {
        return repository.findByTrabajadorId(trabajadorId);
    }

    @Override
    public List<CapacitacionTrabajador> buscarPorCapacitacion(Long capacitacionId) {
        return repository.findByCapacitacionId(capacitacionId);
    }

    public Map<String, CapacitacionTrabajador> mapaEvaluaciones() {

        List<CapacitacionTrabajador> lista = repository.findAll();

        Map<String, CapacitacionTrabajador> mapa = new HashMap<>();

        for (CapacitacionTrabajador e : lista) {

            String key = e.getTrabajador().getId() + "_" + e.getCapacitacion().getId();

            mapa.put(key, e);
        }

        return mapa;
    }
}