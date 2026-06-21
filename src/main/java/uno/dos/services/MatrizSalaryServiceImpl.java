package uno.dos.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uno.dos.dto.CapacitacionHourlyDTO;
import uno.dos.dto.CeldaMatrixDTO;
import uno.dos.dto.CursoHourlyDTO;
import uno.dos.dto.MatrizHourlyDTO;
import uno.dos.dto.TrabajadorMatrixDTO;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.MatrizHabilidad;
import uno.dos.models.entity.MatrizHabilidadCurso;
import uno.dos.models.entity.MatrizHabilidadTrabajador;
import uno.dos.models.entity.NivelSkill;
import uno.dos.repositories.CapacitacionTrabajadorRepository;
import uno.dos.repositories.EvaluacionCapacitacionRepository;
import uno.dos.repositories.MatrizHabilidadRepository;

@Service
@RequiredArgsConstructor
public class MatrizSalaryServiceImpl implements MatrizSalaryService {

    private final MatrizHabilidadRepository matrizRepository;
    private final CapacitacionTrabajadorRepository capacitacionTrabajadorRepository;
    private final EvaluacionCapacitacionRepository evaluacionCapacitacionRepository;

    @Override
    public MatrizHourlyDTO construir(Long matrizId) {

        MatrizHabilidad matriz = matrizRepository.findById(matrizId)
                .orElseThrow(() -> new RuntimeException("Matriz no encontrada"));

        MatrizHourlyDTO dto = new MatrizHourlyDTO();
        dto.setId(matriz.getId());
        dto.setNombre(matriz.getNombre());
        dto.setArea(matriz.getArea().getNombreArea());
        dto.setSubArea(matriz.getSubArea().getNombreSubArea());

        // ============================================
        // CURSOS + CAPACITACIONES
        // ============================================
        List<CursoHourlyDTO> cursos = new ArrayList<>();

        for (MatrizHabilidadCurso mc : matriz.getCursos()) {

            Curso curso = mc.getCurso();

            CursoHourlyDTO cursoDto = new CursoHourlyDTO();
            cursoDto.setId(curso.getId());
            cursoDto.setNombreCurso(curso.getNombreCurso());

            List<CapacitacionHourlyDTO> caps = new ArrayList<>();

            if (curso.getCapacitaciones() != null) {
                for (Capacitacion cap : curso.getCapacitaciones()) {
                    CapacitacionHourlyDTO capDto = new CapacitacionHourlyDTO();
                    capDto.setId(cap.getId());
                    capDto.setNombreCapacitacion(cap.getNombreCapacitacion());
                    caps.add(capDto);
                }
            }

            cursoDto.setCapacitaciones(caps);
            cursoDto.setTotalCapacitaciones(caps.size());
            cursos.add(cursoDto);
        }

        dto.setCursos(cursos);

        // ============================================
        // TRABAJADORES
        // ============================================
        List<TrabajadorMatrixDTO> trabajadores = new ArrayList<>();

        for (MatrizHabilidadTrabajador mt : matriz.getTrabajadores()) {

            TrabajadorMatrixDTO t = new TrabajadorMatrixDTO();

            t.setId(mt.getTrabajador().getId());
            t.setNumeroPersonal(mt.getTrabajador().getNumeroPersonal());
            t.setNombre(mt.getTrabajador().getNombre());
            t.setPrimerApellido(mt.getTrabajador().getPrimerApellido());
            t.setSegundoApellido(mt.getTrabajador().getSegundoApellido());

            if (mt.getTrabajador().getPuesto() != null) {
                t.setPuesto(mt.getTrabajador().getPuesto().getNombrePuesto());
            } else {
                t.setPuesto("Sin puesto");
            }

            t.setFoto(mt.getTrabajador().getFoto());

            int totalCaps = 0;
            int aprobadas = 0;

            for (CursoHourlyDTO cursoDto : cursos) {
                for (CapacitacionHourlyDTO capDto : cursoDto.getCapacitaciones()) {

                    CeldaMatrixDTO celda = new CeldaMatrixDTO();
                    celda.setCapacitacionId(capDto.getId());

                    Long trabajadorId = mt.getTrabajador().getId();
                    Long capacitacionId = capDto.getId();

                    Optional<CapacitacionTrabajador> inscripcionOpt =
                            capacitacionTrabajadorRepository
                                    .findByTrabajadorIdAndCapacitacionId(
                                            trabajadorId,
                                            capacitacionId
                                    );

                    boolean aprobada =
                            evaluacionCapacitacionRepository
                                    .existsByTrabajadorIdAndCapacitacionIdAndAprobadaTrue(
                                            trabajadorId,
                                            capacitacionId
                                    );

                    // =====================================
                    // REGLA SALARY
                    // VERDE = ACREDITADO
                    // AMARILLO = INSCRITO
                    // GRIS = VACIO
                    // =====================================
                    if (aprobada) {
                        celda.setNivel(NivelSkill.ACREDITADO);
                        aprobadas++;

                    } else if (inscripcionOpt.isPresent()) {
                        celda.setNivel(NivelSkill.INSCRITO);

                    } else {
                        celda.setNivel(NivelSkill.VACIO);
                    }

                    t.getCeldas().add(celda);
                    totalCaps++;
                }
            }

            if (totalCaps > 0) {
                t.setPorcentaje((aprobadas * 100) / totalCaps);
            } else {
                t.setPorcentaje(0);
            }

            trabajadores.add(t);
        }

        dto.setTrabajadores(trabajadores);

        return dto;
    }
}