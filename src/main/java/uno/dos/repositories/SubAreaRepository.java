package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uno.dos.models.entity.Area;
import uno.dos.models.entity.SubArea;

@Repository
public interface SubAreaRepository
        extends JpaRepository<SubArea, Long>{

    List<SubArea> findByActivoTrue();

    List<SubArea> findByAreaAndActivoTrue(
            Area area
    );

    boolean existsByNombreSubAreaAndArea(
            String nombreSubArea,
            Area area
    );
}