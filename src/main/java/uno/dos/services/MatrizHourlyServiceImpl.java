package uno.dos.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uno.dos.dto.CapacitacionHourlyDTO;
import uno.dos.dto.CursoHourlyDTO;
import uno.dos.dto.MatrizHourlyDTO;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.MatrizHabilidad;
import uno.dos.models.entity.MatrizHabilidadCurso;
import uno.dos.repositories.MatrizHabilidadRepository;

@Service
@RequiredArgsConstructor
public class MatrizHourlyServiceImpl
        implements MatrizHourlyService {

    private final MatrizHabilidadRepository matrizRepository;

    @Override
    public MatrizHourlyDTO construir(
            Long matrizId) {

        MatrizHabilidad matriz =
                matrizRepository
                .findById(matrizId)
                .orElseThrow();

        MatrizHourlyDTO dto =
                new MatrizHourlyDTO();

        dto.setId(
                matriz.getId()
        );

        dto.setNombre(
                matriz.getNombre()
        );

        dto.setArea(
                matriz.getArea()
                        .getNombreArea()
        );

        dto.setSubArea(
                matriz.getSubArea()
                        .getNombreSubArea()
        );

        List<CursoHourlyDTO> cursos =
                new ArrayList<>();

        for(MatrizHabilidadCurso mc
                : matriz.getCursos()) {

            Curso curso =
                    mc.getCurso();

            CursoHourlyDTO cursoDto =
                    new CursoHourlyDTO();

            cursoDto.setId(
                    curso.getId()
            );

            cursoDto.setNombreCurso(
                    curso.getNombreCurso()
            );

            List<CapacitacionHourlyDTO> caps =
                    new ArrayList<>();

            if(curso.getCapacitaciones() != null){

                for(Capacitacion cap
                        : curso.getCapacitaciones()){

                    CapacitacionHourlyDTO capDto =
                            new CapacitacionHourlyDTO();

                    capDto.setId(
                            cap.getId()
                    );

                    capDto.setNombreCapacitacion(
                            cap.getNombreCapacitacion()
                    );

                    caps.add(
                            capDto
                    );
                }
            }

            cursoDto.setCapacitaciones(
                    caps
            );

            cursoDto.setTotalCapacitaciones(
                    caps.size()
            );

            cursos.add(
                    cursoDto
            );
        }

        dto.setCursos(
                cursos
        );

        return dto;
    }
}