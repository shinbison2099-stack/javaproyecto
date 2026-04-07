package uno.dos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import uno.dos.models.entity.EvaluacionCapacitacion;

public interface EvaluacionCapacitacionRepository
        extends JpaRepository<EvaluacionCapacitacion, Long>{

    List<EvaluacionCapacitacion>
    findByTrabajadorId(Long trabajadorId);

    List<EvaluacionCapacitacion>
    findByCapacitacionId(Long capacitacionId);

    int countByTrabajadorIdAndAprobadaTrue(Long trabajadorId);
    
    List<EvaluacionCapacitacion> findByTrabajador_IdAndCapacitacion_Id(
            Long trabajadorId,
            Long capacitacionId
    );
    
    boolean existsByTrabajador_IdAndCapacitacion_Id(Long trabajadorId, Long capacitacionId);
    
    boolean existsByTrabajadorIdAndCapacitacionIdAndAprobadaTrue(Long trabajadorId, Long capacitacionId);
    
    boolean existsByTrabajador_IdAndCapacitacion_IdAndAprobadaTrue(
    	    Long trabajadorId,
    	    Long capacitacionId
    	);
    
   
}