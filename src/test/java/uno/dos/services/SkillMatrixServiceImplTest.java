package uno.dos.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uno.dos.models.entity.SkillMatrix;
import uno.dos.repositories.SkillMatrixRepository;

@ExtendWith(MockitoExtension.class)
class SkillMatrixServiceImplTest {

    @Mock
    private SkillMatrixRepository skillMatrixRepository;

    @InjectMocks
    private SkillMatrixServiceImpl skillMatrixService;

    private SkillMatrix skill;

    @BeforeEach
    void setUp() {

        skill = new SkillMatrix();
        skill.setId(1L);
    }

    // =====================================
    // 🔥 buscarPorCapacitacion
    // =====================================

    @Test
    void buscarPorCapacitacion_DeberiaRetornarLista() {

        Long capacitacionId = 1L;

        when(skillMatrixRepository
                .findByCapacitacionIdAndActivoTrue(capacitacionId))
                .thenReturn(List.of(skill));

        List<SkillMatrix> resultado =
                skillMatrixService.buscarPorCapacitacion(capacitacionId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(skillMatrixRepository)
                .findByCapacitacionIdAndActivoTrue(capacitacionId);
    }

    // =====================================
    // 🔥 buscarPorTrabajador
    // =====================================

    @Test
    void buscarPorTrabajador_DeberiaRetornarLista() {

        Long trabajadorId = 1L;

        when(skillMatrixRepository
                .findByTrabajadorIdAndActivoTrue(trabajadorId))
                .thenReturn(List.of(skill));

        List<SkillMatrix> resultado =
                skillMatrixService.buscarPorTrabajador(trabajadorId);

        assertEquals(1, resultado.size());

        verify(skillMatrixRepository)
                .findByTrabajadorIdAndActivoTrue(trabajadorId);
    }

    // =====================================
    // 🔥 buscarPorId
    // =====================================

    @Test
    void buscarPorId_DeberiaRetornarOptional() {

        when(skillMatrixRepository.findById(1L))
                .thenReturn(Optional.of(skill));

        Optional<SkillMatrix> resultado =
                skillMatrixService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());

        verify(skillMatrixRepository).findById(1L);
    }

    // =====================================
    // 🔥 guardar
    // =====================================

    @Test
    void guardar_DeberiaGuardarSkill() {

        skillMatrixService.guardar(skill);

        verify(skillMatrixRepository).save(skill);
    }

    // =====================================
    // 🔥 buscarPorTrabajadorYCapacitacion
    // =====================================

    @Test
    void buscarPorTrabajadorYCapacitacion_DeberiaRetornarSkill() {

        Long trabajadorId = 1L;
        Long capacitacionId = 2L;

        when(skillMatrixRepository
                .findByTrabajadorIdAndCapacitacionId(
                        trabajadorId,
                        capacitacionId))
                .thenReturn(Optional.of(skill));

        Optional<SkillMatrix> resultado =
                skillMatrixService
                        .buscarPorTrabajadorYCapacitacion(
                                trabajadorId,
                                capacitacionId
                        );

        assertTrue(resultado.isPresent());

        verify(skillMatrixRepository)
                .findByTrabajadorIdAndCapacitacionId(
                        trabajadorId,
                        capacitacionId
                );
    }
}