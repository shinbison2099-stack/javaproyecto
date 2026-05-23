package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import uno.dos.models.entity.SkillMatrix;
import uno.dos.repositories.SkillMatrixRepository;
import uno.dos.services.SkillMatrixService;

@Service
@RequiredArgsConstructor
public class SkillMatrixServiceImpl
        implements SkillMatrixService{

    private final SkillMatrixRepository skillMatrixRepository;

    // =====================================
    // 🔥 POR CAPACITACIÓN
    // =====================================

    @Override
    public List<SkillMatrix>
    buscarPorCapacitacion(Long capacitacionId){

        return skillMatrixRepository
                .findByCapacitacionIdAndActivoTrue(
                        capacitacionId
                );
    }

    // =====================================
    // 🔥 POR TRABAJADOR
    // =====================================

    @Override
    public List<SkillMatrix>
    buscarPorTrabajador(Long trabajadorId){

        return skillMatrixRepository
                .findByTrabajadorIdAndActivoTrue(
                        trabajadorId
                );
    }

    // =====================================
    // 🔥 BUSCAR ID
    // =====================================

    @Override
    public Optional<SkillMatrix>
    buscarPorId(Long id){

        return skillMatrixRepository.findById(id);
    }

    // =====================================
    // 🔥 GUARDAR
    // =====================================

    @Override
    public void guardar(SkillMatrix skill){

        skillMatrixRepository.save(skill);
    }

    // =====================================
    // 🔥 BUSCAR UNO
    // =====================================

    @Override
    public Optional<SkillMatrix>
    buscarPorTrabajadorYCapacitacion(

            Long trabajadorId,
            Long capacitacionId){

        return skillMatrixRepository
                .findByTrabajadorIdAndCapacitacionId(

                        trabajadorId,
                        capacitacionId
                );
    }

}