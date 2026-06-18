package uno.dos.dto;

import lombok.Data;

@Data
public class HabilidadSalaryDTO {

    private Long habilidadId;

    private String nombreHabilidad;

    private Integer nivelActual;

    private Integer nivelObjetivo;

    private Boolean completada;

    private String color;
}
