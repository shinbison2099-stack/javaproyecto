package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByCursoId(Long cursoId);

}