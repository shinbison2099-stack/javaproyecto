package uno.dos.services;

import uno.dos.models.entity.CapacitacionTrabajador;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CapacitacionTrabajadorService {

    List<CapacitacionTrabajador> listarTodas();

    List<CapacitacionTrabajador> listarActivas();

    Optional<CapacitacionTrabajador> buscarPorId(Long id);

    CapacitacionTrabajador guardar(CapacitacionTrabajador ct);

    void eliminar(Long id);

    // 🔥 EXTRA (clave para matriz)
    List<CapacitacionTrabajador> buscarPorTrabajador(Long trabajadorId);

    List<CapacitacionTrabajador> buscarPorCapacitacion(Long capacitacionId);
    
    public Map<String, CapacitacionTrabajador> mapaEvaluaciones();

}