package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.MatrizHabilidad;

public interface MatrizHabilidadRepository
        extends JpaRepository<MatrizHabilidad, Long>{

    List<MatrizHabilidad>
    findByActivoTrue();

}