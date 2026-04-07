package uno.dos.models.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
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
@Table(name = "capacitaciontrabajador")
public class CapacitacionTrabajador {

		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    private Trabajador trabajador;

	    @ManyToOne
	    private Capacitacion capacitacion;

	    private LocalDate fechaInscripcion;

	    private Double calificacion;

	    private Boolean aprobado;
	    
	 // 👇 NUEVO
	    private Boolean activo = true;
	    
	    @Column
	    private Integer nivel;
	    
	    private Integer orden;        // 🔥 orden del curso
	    private boolean completado;   // 🔥 ya terminó

}

