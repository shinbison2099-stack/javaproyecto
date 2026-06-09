package uno.dos.models.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "capacitaciontrabajador")
public class CapacitacionTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================
    // TRABAJADOR
    // =====================================

    @ManyToOne
    @JsonIgnore
    private Trabajador trabajador;

    // =====================================
    // CAPACITACION
    // =====================================

    @ManyToOne
    @JsonIgnore
    private Capacitacion capacitacion;

    // =====================================
    // FECHAS
    // =====================================

    private LocalDate fechaInscripcion;

    /**
     * Fecha en la que el trabajador acreditó
     * la capacitación.
     */
    private LocalDate fechaAcreditacion;

    // =====================================
    // RESULTADOS
    // =====================================

    private Double calificacion;

    private Boolean aprobado = false;

    private Boolean completado = false;

    // =====================================
    // MATRICES
    // =====================================

    @Column
    private Integer nivel;

    private Integer orden;

    // =====================================
    // ESTATUS
    // =====================================

    private Boolean activo = true;

    // =====================================
    // FECHA VENCIMIENTO
    // =====================================

    @Transient
    public LocalDate getFechaVencimiento(){

        if(fechaAcreditacion == null){
            return null;
        }

        if(capacitacion == null){
            return null;
        }

        if(capacitacion.getCurso() == null){
            return null;
        }

        if(capacitacion.getCurso().getVigenciaMeses() == null){
            return null;
        }

        return fechaAcreditacion.plusMonths(
                capacitacion
                .getCurso()
                .getVigenciaMeses()
        );
    }

    // =====================================
    // VENCIDO
    // =====================================

    @Transient
    public boolean isVencido(){

        LocalDate vencimiento =
                getFechaVencimiento();

        return vencimiento != null
                &&
                vencimiento.isBefore(
                        LocalDate.now()
                );
    }
}