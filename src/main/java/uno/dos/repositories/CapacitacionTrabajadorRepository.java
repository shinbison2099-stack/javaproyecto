package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.models.entity.Trabajador;

public interface CapacitacionTrabajadorRepository extends JpaRepository<CapacitacionTrabajador, Long> {

List<CapacitacionTrabajador> findByCapacitacionId(Long id);

boolean existsByTrabajador_IdAndCapacitacion_Id(Long trabajadorId, Long capacitacionId);

void deleteByTrabajador_IdAndCapacitacion_Id(Long trabajadorId, Long capacitacionId);

List<CapacitacionTrabajador> findByCapacitacionIdAndActivoTrue(Long id);

List<CapacitacionTrabajador> findByCapacitacionIdAndActivoFalse(Long id);

Optional<CapacitacionTrabajador> findByTrabajador_IdAndCapacitacion_Id(Long trabajadorId, Long capacitacionId);

Optional<CapacitacionTrabajador> findByTrabajadorIdAndCapacitacionId(
	    Long trabajadorId,
	    Long capacitacionId
	);

Optional<CapacitacionTrabajador> 
findFirstByTrabajador_IdAndCompletadoFalseOrderByOrdenAsc(Long trabajadorId);

List<CapacitacionTrabajador>
findByTrabajador_IdAndCompletadoFalse(Long trabajadorId);

List<CapacitacionTrabajador> findByActivoTrue();

List<CapacitacionTrabajador> findByTrabajadorId(Long trabajadorId);

List<CapacitacionTrabajador> findByCapacitacion_Id(Long capId);

List<CapacitacionTrabajador> findByTrabajador_IdAndActivoTrue(Long trabajadorId);





}