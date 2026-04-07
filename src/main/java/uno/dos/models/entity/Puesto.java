package uno.dos.models.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "puesto")
public class Puesto {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String nombrePuesto;

	    // Nivel jerárquico
	    private Integer nivel;

	    // orden dentro del área
	    private Integer ordenAscenso;

	    // activo o eliminado lógico
	    private Boolean activo;
	    
	    @Transient
	    private List<Capacitacion> capacitaciones;

	    @ManyToOne
	    private Area area;
	    
	    @OneToMany(mappedBy = "puesto")
	    private List<PuestoCapacitacion> puestoCapacitaciones;
	    
	    @Transient
	    public List<Capacitacion> getCapacitaciones() {

	        if (puestoCapacitaciones == null) return List.of();

	        return puestoCapacitaciones.stream()
	                .map(PuestoCapacitacion::getCapacitacion)
	                .toList();
	    }
}