package uno.dos.dto;

import lombok.Data;

@Data
public class CapacitacionHourlyDTO {

    private Long id;

    private String nombreCapacitacion;

    private Integer duracionHoras;

    private Integer inscritos;

    private Integer aprobados;
    
 // opcional pero útil para la tabla
    private Long cursoId;
    private String nombreCurso;
}