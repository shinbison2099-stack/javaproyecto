package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.MatrizHabilidadNivel;

@Repository
public interface MatrizHabilidadNivelRepository
        extends JpaRepository<MatrizHabilidadNivel, Long>{

    List<MatrizHabilidadNivel>
    findByTrabajadorId(Long trabajadorId);

    List<MatrizHabilidadNivel>
    findByMatrizId(Long matrizId);

}