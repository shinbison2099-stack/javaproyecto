package uno.dos.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TrabajadorMatrixDTO {

    private Long id;
    private String numeroPersonal;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String puesto;
    private String foto;

    private Integer porcentaje = 0;

    private List<CeldaMatrixDTO> celdas = new ArrayList<>();
}