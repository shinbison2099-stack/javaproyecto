package uno.dos.models.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "evaluarcapacitacion")
public class EvaluacionCapacitacion {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    private Trabajador trabajador;

	    @ManyToOne
	    private Capacitacion capacitacion;

	    private Integer calificacion;

	    private Boolean aprobada;

	    private LocalDate fechaEvaluacion;
	    
	   

}
