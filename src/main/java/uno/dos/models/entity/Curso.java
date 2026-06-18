package uno.dos.models.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String claveInstitucion;

    @Column(unique = true)
    private String claveCurso;

    private String nombreCurso;

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

    // =====================================
    // CAPACITACIONES
    // =====================================

    @OneToMany(mappedBy = "curso")
    @JsonIgnore
    private List<Capacitacion> capacitaciones =
            new ArrayList<>();

    // =====================================
    // PUESTOS
    // =====================================

    @OneToMany(mappedBy = "curso")
    @JsonIgnore
    private List<PuestoCurso> puestoCursos =
            new ArrayList<>();

    // =====================================
    // SOLO PARA VISTAS
    // =====================================

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

    // =====================================
    // TOTAL CAPACITACIONES
    // =====================================

    @Transient
    public int getTotalCapacitaciones() {

        return capacitaciones != null
                ? capacitaciones.size()
                : 0;
    }

    // =====================================
    // TOTAL INSCRITOS
    // =====================================

    @Transient
    public int getTotalInscritos() {

        if (capacitaciones == null) {
            return 0;
        }

        return capacitaciones.stream()

                .filter(c ->
                        c.getCapacitacionTrabajadores()
                                != null)

                .mapToInt(c ->
                        c.getCapacitacionTrabajadores()
                                .size())

                .sum();
    }

    // =====================================
    // HORAS USADAS
    // =====================================

    @Transient
    public int getHorasUsadas() {

        if (capacitaciones == null) {
            return 0;
        }

        return capacitaciones.stream()

                .mapToInt(c ->
                        c.getDuracionHoras() != null
                                ? c.getDuracionHoras()
                                : 0)

                .sum();
    }

    // =====================================
    // HORAS RESTANTES
    // =====================================

    @Transient
    public int getHorasRestantes() {

        if (horas == null) {
            return 0;
        }

        return horas - getHorasUsadas();
    }

    // =====================================
    // CALCULAR VENCIMIENTO
    // =====================================

    @Transient
    public LocalDate calcularVencimiento(
            LocalDate fechaAcreditacion) {

        if (fechaAcreditacion == null
                || vigenciaMeses == null) {

            return null;
        }

        return fechaAcreditacion
                .plusMonths(vigenciaMeses);
    }
}