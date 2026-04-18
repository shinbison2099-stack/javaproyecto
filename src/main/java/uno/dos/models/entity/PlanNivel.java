package uno.dos.models.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "plan_nivel")
public class PlanNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Integer orden;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private PlanCarrera plan;
    
    // 🔥 AGREGAR ESTO
    @ManyToOne
    @JoinColumn(name = "puesto_id")
    private Puesto puesto;
    
    @ManyToMany
    @JoinTable(
        name = "plan_nivel_curso",
        joinColumns = @JoinColumn(name = "nivel_id"),
        inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private List<Curso> cursos;
}