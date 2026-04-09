package uno.dos.models.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "matrices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Matriz {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(nullable = false)
    private String nombre;

    @ManyToMany
    @JoinTable(
        name = "matriz_trabajador",
        joinColumns = @JoinColumn(name = "matriz_id"),
        inverseJoinColumns = @JoinColumn(name = "trabajador_id")
    )
    private List<Trabajador> trabajadores;

    @ManyToMany
    @JoinTable(
        name = "matriz_capacitacion",
        joinColumns = @JoinColumn(name = "matriz_id"),
        inverseJoinColumns = @JoinColumn(name = "capacitacion_id")
    )
    private List<Capacitacion> capacitaciones;
}