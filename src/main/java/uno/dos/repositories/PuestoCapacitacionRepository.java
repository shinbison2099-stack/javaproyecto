package uno.dos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import uno.dos.models.entity.PuestoCapacitacion;

public interface PuestoCapacitacionRepository
        extends JpaRepository<PuestoCapacitacion, Long> {

    List<PuestoCapacitacion> findByPuestoId(Long puestoId);
    
    @Query("""
    	    SELECT pc 
    	    FROM PuestoCapacitacion pc
    	    JOIN FETCH pc.capacitacion
    	    WHERE pc.puesto.id IN :ids
    	""")
    	List<PuestoCapacitacion> findAllByPuestoIds(@Param("ids") List<Long> ids);

}