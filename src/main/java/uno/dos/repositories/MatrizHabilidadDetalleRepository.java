package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.MatrizHabilidadDetalle;

@Repository
public interface MatrizHabilidadDetalleRepository
        extends JpaRepository<MatrizHabilidadDetalle, Long>{

    List<MatrizHabilidadDetalle>
    findByMatrizId(Long matrizId);

    void deleteByMatrizId(Long matrizId);

}