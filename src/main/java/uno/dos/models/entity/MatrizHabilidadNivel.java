package uno.dos.models.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matriz_habilidad_nivel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrizHabilidadNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MatrizHabilidad matriz;

    @ManyToOne
    private Trabajador trabajador;

    @ManyToOne
    private Habilidad habilidad;

    private Integer nivelActual;

    private Integer nivelObjetivo;

    private Boolean completada;
}