package uno.dos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.repositories.EvaluacionCapacitacionRepository;

@Service
@RequiredArgsConstructor
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionCapacitacionRepository repository;
    

    public void guardar(EvaluacionCapacitacion evaluacion){
        repository.save(evaluacion);
    }

    public List<EvaluacionCapacitacion> listarPorTrabajador(Long trabajadorId){
        return repository.findByTrabajadorId(trabajadorId);
    }

    public List<EvaluacionCapacitacion> listarPorCapacitacion(Long capacitacionId){
        return repository.findByCapacitacionId(capacitacionId);
    }

    public int contarAprobadas(Long trabajadorId){
        return repository.countByTrabajadorIdAndAprobadaTrue(trabajadorId);
    }
    
    @Override
    public EvaluacionCapacitacion buscarPorTrabajadorYCapacitacion(Long trabajadorId, Long capacitacionId){
        return repository
                .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, capacitacionId)
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public List<EvaluacionCapacitacion> 
    buscarPorCapacitaciones(List<Long> capacitacionIds){

        return repository.findByCapacitacionIdIn(capacitacionIds);
    }
    
   
    
}