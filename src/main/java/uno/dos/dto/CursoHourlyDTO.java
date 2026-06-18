package uno.dos.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CursoHourlyDTO {

    private Long id;

    private String nombreCurso;

    private Integer horas;

    private Integer horasUsadas;

    private Integer horasRestantes;

    private Integer totalCapacitaciones;

    private Integer totalAlumnos;

    private String tipoTrabajador;

    private List<CapacitacionHourlyDTO> capacitaciones =
            new ArrayList<>();
}