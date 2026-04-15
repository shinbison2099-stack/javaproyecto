package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Inscripcion;
import uno.dos.models.entity.Trabajador;

@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {

    Optional<Trabajador> findByEmail(String email);
    boolean existsByCurp(String curp);
    List<Trabajador> findByActivoTrue();

    List<Trabajador> findByActivoFalse();
    
   

}