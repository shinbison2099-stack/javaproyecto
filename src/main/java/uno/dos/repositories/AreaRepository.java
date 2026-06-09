package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Area;

@Repository
public interface AreaRepository
        extends JpaRepository<Area, Long>{

    List<Area> findByActivoTrue();

    List<Area> findByActivoFalse();

    boolean existsByNombreArea(String nombreArea);
}