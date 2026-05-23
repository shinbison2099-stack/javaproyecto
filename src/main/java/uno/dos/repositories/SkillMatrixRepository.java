package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.SkillMatrix;

public interface SkillMatrixRepository
        extends JpaRepository<SkillMatrix, Long>{

    // =====================================
    // 🔥 POR CAPACITACIÓN
    // =====================================

    List<SkillMatrix>
    findByCapacitacionIdAndActivoTrue(Long id);

    // =====================================
    // 🔥 POR TRABAJADOR
    // =====================================

    List<SkillMatrix>
    findByTrabajadorIdAndActivoTrue(Long id);

    // =====================================
    // 🔥 BUSCAR UNO
    // =====================================

    Optional<SkillMatrix>
    findByTrabajadorIdAndCapacitacionId(
            Long trabajadorId,
            Long capacitacionId
    );

}