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

    // 🔥 NUEVO: HORAS DEL CURSO
    @Column(name = "horas")
    private Integer horas;

    // 🔥 NUEVO: VIGENCIA DEL CURSO (MESES)
    @Column(name = "vigencia_meses")
    private Integer vigenciaMeses;

    @Enumerated(EnumType.STRING)
    private TipoCurso tipoCurso;

    @Enumerated(EnumType.STRING)
    private TipoTrabajador tipoTrabajador;

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

    // 🔥 RELACIÓN CON PUESTO_CURSO
    @OneToMany(mappedBy = "curso")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<PuestoCurso> puestoCursos;

    // 🔥 SOLO PARA VISTA
    @Transient
    public List<Puesto> getPuestos() {
        return puestoCursos.stream()
                .map(PuestoCurso::getPuesto)
                .toList();
    }

    // ===============================
    // CAPACITACIONES
    // ===============================

    @OneToMany(mappedBy = "curso")
    @JsonIgnore
    private List<Capacitacion> capacitaciones = new ArrayList<>();

    @Transient
    public String getNombre() {
        return nombreCurso;
    }

    // 🔥 HORAS USADAS
    @Transient
    public int getHorasUsadas(){
        return capacitaciones.stream()
            .mapToInt(c -> c.getDuracionHoras() != null ? c.getDuracionHoras() : 0)
            .sum();
    }

    // 🔥 HORAS RESTANTES (YA CORRECTO)
    @Transient
    public int getHorasRestantes(){
        return horas != null ? horas - getHorasUsadas() : 0;
    }

    // 🔥 TOTAL INSCRITOS
    @Transient
    public int getTotalInscritos(){
        return capacitaciones.stream()
            .flatMap(cap -> cap.getCapacitacionTrabajadores().stream())
            .mapToInt(ct -> ct.getActivo() != null && ct.getActivo() ? 1 : 0)
            .sum();
    }

    // 🔥 NUEVO: CALCULAR VENCIMIENTO
    @Transient
    public LocalDate calcularVencimiento(LocalDate fechaFinCapacitacion){
        if(fechaFinCapacitacion == null || vigenciaMeses == null){
            return null;
        }
        return fechaFinCapacitacion.plusMonths(vigenciaMeses);
    }

    @Column(length = 1000)
    private String comentarios;
    
    
}