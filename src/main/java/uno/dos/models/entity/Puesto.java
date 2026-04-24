package uno.dos.models.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
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

    private Integer nivel;
    private Integer ordenAscenso;
    private Boolean activo;

    @ManyToOne
    private Area area;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaPuesto categoria;

    // 🔥 RELACIÓN CORRECTA
    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PuestoCurso> puestoCursos = new ArrayList<>();

    // 🔥 SOLO PARA USO EN VISTA (NO BD)
    @Transient
    public List<Curso> getCursos() {
        return puestoCursos.stream()
                .map(PuestoCurso::getCurso)
                .toList();
    }

    // 🔥 CAPACITACIONES (lo dejas igual)
    @OneToMany(mappedBy = "puesto")
    private List<PuestoCapacitacion> puestoCapacitaciones;

    @Transient
    public List<Capacitacion> getCapacitaciones() {

        if (puestoCapacitaciones == null) return List.of();

        return puestoCapacitaciones.stream()
                .map(PuestoCapacitacion::getCapacitacion)
                .toList();
    }

    @Override
    public String toString() {
        return nombrePuesto;
    }
    
    @Transient
    private List<Capacitacion> capacitaciones;
}