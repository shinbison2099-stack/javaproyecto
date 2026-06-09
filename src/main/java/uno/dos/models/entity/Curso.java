package uno.dos.models.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "cursos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave_institucion")
    private String claveInstitucion;

    @Column(name = "clave_curso", unique = true)
    private String claveCurso;

    private String nombreCurso;

    @Column(name = "clave_area_tematica")
    private String claveAreaTematica;

    private Integer horas;

    private Integer vigenciaMeses;

    @Enumerated(EnumType.STRING)
    private TipoCurso tipoCurso;

    @Enumerated(EnumType.STRING)
    private TipoTrabajador tipoTrabajador;

    private String areaResponsable;

    private Boolean activo = true;

    @Column(length = 1000)
    private String comentarios;

    @OneToMany(mappedBy = "curso")
    @JsonIgnore
    private List<Capacitacion> capacitaciones = new ArrayList<>();

    @OneToMany(mappedBy = "curso")
    @JsonIgnore
    private List<PuestoCurso> puestoCursos;

    @Transient
    public String getNombre() {
        return nombreCurso;
    }

    @Transient
    public List<Puesto> getPuestos() {

        return puestoCursos.stream()
                .map(PuestoCurso::getPuesto)
                .toList();
    }
}