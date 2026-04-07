package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Asistencia;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByCursoId(Long cursoId);

    List<Asistencia> findByTrabajadorId(Long trabajadorId);

}