package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Rol;
import uno.dos.models.entity.Instructores;

@Repository
public interface InstructorRepository extends JpaRepository<Instructores, Long> {

	Optional<Instructores> findByEmail(String email);

    List<Instructores> findByRol(Rol rol);
    
    boolean existsByEmail(String email);

	 List<Instructores> findByActivoFalse();
    
	List<Instructores> findByActivoTrue();
    
	Optional<Instructores> findByNombre(String nombre);
	
	List<Instructores> findByActivoTrueOrderByNombreAsc();
	
}