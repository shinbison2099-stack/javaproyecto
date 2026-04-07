package uno.dos.services;

import java.util.List;
import uno.dos.models.entity.EvaluacionCapacitacion;

public interface EvaluacionService {

    void guardar(EvaluacionCapacitacion evaluacion);

    List<EvaluacionCapacitacion> listarPorTrabajador(Long trabajadorId);

    List<EvaluacionCapacitacion> listarPorCapacitacion(Long capacitacionId);

    int contarAprobadas(Long trabajadorId);
    
    public EvaluacionCapacitacion buscarPorTrabajadorYCapacitacion(Long trabajadorId, Long capacitacionId);
    
    


}