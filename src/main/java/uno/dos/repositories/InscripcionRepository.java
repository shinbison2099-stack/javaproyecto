package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Inscripcion;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByTrabajador_Id(Long trabajadorId);

    List<Inscripcion> findByCurso_Id(Long cursoId);

    boolean existsByTrabajador_IdAndCurso_Id(Long trabajadorId, Long cursoId);
    
    List<Inscripcion> findByTrabajadorId(Long trabajadorId);
    
    void deleteByTrabajador_IdAndCurso_Id(Long trabajadorId, Long cursoId);

	Optional<Inscripcion> findByTrabajador_IdAndCurso_Id(Long trabajadorId, Long cursoId);
	
	
	

}