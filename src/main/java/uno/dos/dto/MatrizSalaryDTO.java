package uno.dos.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;
import uno.dos.models.entity.Trabajador;

@Data
public class MatrizSalaryDTO {

    private Trabajador trabajador;

    private List<HabilidadSalaryDTO> habilidades;

    private Integer porcentaje;
}