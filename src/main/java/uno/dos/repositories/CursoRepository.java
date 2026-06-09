package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Curso;
import uno.dos.models.entity.TipoTrabajador;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

	

	List<Curso> findByActivoTrue();

	List<Curso> findByActivoFalse();

    boolean existsByClaveCurso(String claveCurso);
    
    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.capacitaciones WHERE c.activo = true")
    List<Curso> findActivosConCapacitaciones();
    
    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.capacitaciones")
    List<Curso> listarConCapacitaciones();

    Optional<Curso> findByClaveCurso(String claveCurso);
    
    @Query("""
    		SELECT c
    		FROM Curso c
    		WHERE TRIM(UPPER(c.claveCurso))
    		=
    		TRIM(UPPER(:clave))
    		""")
    		Curso buscarPorClaveNormalizada(
    		        @Param("clave") String clave
    		);
    
    @Query("""
    		SELECT c
    		FROM Curso c
    		WHERE TRIM(UPPER(c.nombreCurso))
    		=
    		TRIM(UPPER(:nombre))
    		""")
    		Curso buscarPorNombreNormalizado(
    		        @Param("nombre") String nombre
    		);
    
    List<Curso>
    findByActivoTrueAndTipoTrabajador(
            TipoTrabajador tipo
    );
}