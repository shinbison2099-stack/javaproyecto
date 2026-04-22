package uno.dos.models.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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

    private Integer duracion; // horas del curso

    // 🔥 NUEVO
    @Enumerated(EnumType.STRING)
    private TipoCurso tipoCurso;

    // 🔥 NUEVO
    @Enumerated(EnumType.STRING)
    private TipoTrabajador tipoTrabajador;

    // 🔥 NUEVO
    private String areaResponsable;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructores instructor;

    private boolean activo;

    @Transient
    public String getNombre() {
        return nombreCurso;
    }

    @OneToMany(mappedBy = "curso")
    @JsonIgnore
    private List<Capacitacion> capacitaciones = new ArrayList<>();

    @Transient
    public int getHorasUsadas(){
        return capacitaciones.stream()
            .mapToInt(c -> c.getDuracionHoras() != null ? c.getDuracionHoras() : 0)
            .sum();
    }

    @Transient
    public int getHorasRestantes(){
        return duracion != null ? duracion - getHorasUsadas() : 0;
    }

    @Transient
    public int getTotalInscritos(){
        return capacitaciones.stream()
            .flatMap(cap -> cap.getCapacitacionTrabajadores().stream())
            .mapToInt(ct -> ct.getActivo() != null && ct.getActivo() ? 1 : 0)
            .sum();
    }
    
    @Column(length = 1000)
    private String comentarios;
}