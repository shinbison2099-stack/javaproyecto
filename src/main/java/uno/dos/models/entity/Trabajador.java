package uno.dos.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trabajador")
public class Trabajador {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name="numero_personal", unique = true)
	    private String numeroPersonal;

	    @Column(unique = true, length = 18)
	    private String curp;

	    private String nombre;

	    private String primerApellido;

	    private String segundoApellido;

	    private String areaEmpleado;

	    @ManyToOne
	    @JoinColumn(name="puesto_id")
	    private Puesto puesto;

	    private String claveEstado;

	    private String claveMunicipio;

	    private String claveOcupacion;

	    private String claveNivelEstudios;

	    @Column(name="clave_documento_probatorio")
	    private String claveDocumentoProbatorio;

	    @Column(unique = true)
	    private String email;

	    private String foto;

	    private Boolean activo = true;

}