package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.TipoTrabajador;

public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {

	@Query("SELECT c FROM Capacitacion c WHERE c.activo = true")
	List<Capacitacion> listarActivas();


boolean existsByClaveCapacitacion(String claveCapacitacion);

@Modifying
@Query("UPDATE Capacitacion c SET c.activo = true WHERE c.id = :id")
void restaurar(@Param("id") Long id);

List<Capacitacion> findByActivoTrue();
List<Capacitacion> findByActivoFalse();

List<Capacitacion> findByCursoIdAndActivoTrue(Long cursoId);

List<Capacitacion> findByCursoIdInAndActivoTrue(List<Long> cursoIds);

void deleteById(Long id);
boolean existsById(Long id);

List<Capacitacion> findByCursoIsNull();

List<Capacitacion> findByCursoIsNullAndTipoTrabajadorIn(
        List<TipoTrabajador> tipos
);

// 🔥 NUEVO
List<Capacitacion>
findByCursoIsNullAndActivoTrueAndTipoTrabajador(
        TipoTrabajador tipoTrabajador
);

}