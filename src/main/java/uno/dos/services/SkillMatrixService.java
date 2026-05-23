package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.SkillMatrix;

public interface SkillMatrixService {

    // =====================================
    // 🔥 LISTAR POR CAPACITACIÓN
    // =====================================

    List<SkillMatrix>
    buscarPorCapacitacion(Long capacitacionId);

    // =====================================
    // 🔥 LISTAR POR TRABAJADOR
    // =====================================

    List<SkillMatrix>
    buscarPorTrabajador(Long trabajadorId);

    // =====================================
    // 🔥 BUSCAR POR ID
    // =====================================

    Optional<SkillMatrix>
    buscarPorId(Long id);

    // =====================================
    // 🔥 GUARDAR
    // =====================================

    void guardar(SkillMatrix skill);

    // =====================================
    // 🔥 BUSCAR UNO
    // =====================================

    Optional<SkillMatrix>
    buscarPorTrabajadorYCapacitacion(
            Long trabajadorId,
            Long capacitacionId
    );

}