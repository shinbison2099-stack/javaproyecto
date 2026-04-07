package uno.dos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import uno.dos.models.entity.PuestoCapacitacion;

public interface PuestoCapacitacionRepository
        extends JpaRepository<PuestoCapacitacion, Long> {

    List<PuestoCapacitacion> findByPuestoId(Long puestoId);

}