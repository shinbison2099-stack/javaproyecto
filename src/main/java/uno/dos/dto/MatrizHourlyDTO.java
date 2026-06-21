package uno.dos.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MatrizHourlyDTO {

    private Long id;
    private String nombre;
    private String area;
    private String subArea;

    private List<CursoHourlyDTO> cursos = new ArrayList<>();

    // 🔥 lista plana para pintar la tabla
    private List<CapacitacionHourlyDTO> capacitaciones = new ArrayList<>();

    private List<TrabajadorMatrixDTO> trabajadores = new ArrayList<>();
}