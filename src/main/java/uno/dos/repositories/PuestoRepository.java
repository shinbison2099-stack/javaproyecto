package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.Puesto;

public interface PuestoRepository extends JpaRepository<Puesto, Long>{

    List<Puesto> findByActivoTrue();

    List<Puesto> findByActivoFalse();

    boolean existsByNombrePuesto(String nombrePuesto);
    
    Optional<Puesto> findByNombrePuesto(String nombrePuesto);
    
    List<Puesto> findAllByOrderByNivelAsc();

}