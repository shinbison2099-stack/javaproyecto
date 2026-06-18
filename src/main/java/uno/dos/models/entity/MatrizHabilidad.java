package uno.dos.models.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matriz_habilidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrizHabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne
    private Area area;

    @ManyToOne
    private SubArea subArea;

    @Enumerated(EnumType.STRING)
    private TipoTrabajador tipoTrabajador;

    private Boolean activo = true;

    @OneToMany(
            mappedBy = "matriz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MatrizHabilidadDetalle> detalles =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "matriz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MatrizHabilidadCurso> cursos =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "matriz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MatrizHabilidadTrabajador> trabajadores =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "matriz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MatrizHabilidadNivel> niveles =
            new ArrayList<>();
}
