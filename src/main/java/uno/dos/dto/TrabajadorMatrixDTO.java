package uno.dos.dto;

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
}