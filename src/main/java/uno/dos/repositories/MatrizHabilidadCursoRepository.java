package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.MatrizHabilidadCurso;

@Repository
public interface MatrizHabilidadCursoRepository
        extends JpaRepository<MatrizHabilidadCurso, Long>{

    List<MatrizHabilidadCurso>
    findByMatrizId(Long matrizId);

    void deleteByMatrizId(Long matrizId);

}