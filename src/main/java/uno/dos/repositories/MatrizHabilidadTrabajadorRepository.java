package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.MatrizHabilidadTrabajador;

@Repository
public interface MatrizHabilidadTrabajadorRepository
        extends JpaRepository<MatrizHabilidadTrabajador, Long>{

    List<MatrizHabilidadTrabajador>
    findByMatrizId(Long matrizId);

    void deleteByMatrizId(Long matrizId);

}