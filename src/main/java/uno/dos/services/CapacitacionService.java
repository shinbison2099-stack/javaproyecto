package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.Capacitacion;

public interface CapacitacionService {

    List<Capacitacion> listarActivas();

    Optional<Capacitacion> buscarPorId(Long id);

    Capacitacion guardar(Capacitacion capacitacion);

    void eliminar(Long id);
    
    public List<Capacitacion> listarEliminadas();
    
    public boolean existeClave(String clave);
    
    public void restaurarCapacitacion(Long id);
    
    List<Capacitacion> buscarPorIds(List<Long> ids);

}