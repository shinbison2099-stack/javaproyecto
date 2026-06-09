package uno.dos.models.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "capacitaciones")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Capacitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String claveCapacitacion;

    private String nombreCapacitacion;

    @Column(length = 1000)
    private String descripcion;

    private String tipoCapacitacion;

    private String modalidad;

    private Integer duracionHoras;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_trabajador")
    private TipoTrabajador tipoTrabajador;

    private Boolean requiereEvaluacion = false;

    private Boolean requiereConstancia = false;

    private Boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructores instructor;

    @OneToMany(mappedBy = "capacitacion")
    @JsonIgnore
    private List<CapacitacionTrabajador> capacitacionTrabajadores =
            new ArrayList<>();
}