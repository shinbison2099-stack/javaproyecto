package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

	

	List<Curso> findByActivoTrue();

	List<Curso> findByActivoFalse();

    List<Curso> findByInstructorId(Long instructorId);
    
    boolean existsByClaveCurso(String claveCurso);
    
    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.capacitaciones WHERE c.activo = true")
    List<Curso> findActivosConCapacitaciones();
    
    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.capacitaciones")
    List<Curso> listarConCapacitaciones();

}