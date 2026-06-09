package uno.dos.models.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "skill_matrix")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================
    // 🔥 TRABAJADOR
    // =====================================

    @ManyToOne
    @JoinColumn(name = "trabajador_id")
    @JsonIgnore
    private Trabajador trabajador;

    // =====================================
    // 🔥 CAPACITACIÓN
    // =====================================

    @ManyToOne
    @JoinColumn(name = "capacitacion_id")
    @JsonIgnore
    private Capacitacion capacitacion;

    // =====================================
    // 🔥 NIVEL ACTUAL
    // =====================================

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_skill")
    private NivelSkill nivel = NivelSkill.VACIO;

    /*
        VACIO
        PROGRAMADO
        CAPACITADO
        EXPERIENCIA_40H
        ACREDITADO
    */

    // =====================================
    // 🔥 PROGRAMADO
    // =====================================

    private LocalDate fechaProgramada;

    private String evidenciaProgramado;

    // =====================================
    // 🔥 CAPACITADO
    // =====================================

    private LocalDate fechaCapacitado;

    private String evidenciaCapacitado;

    // =====================================
    // 🔥 EXPERIENCIA 40H
    // =====================================

    private LocalDate fechaExperiencia;

    private String evidenciaExperiencia;

    @Column(name = "horas_experiencia")
    private Integer horasExperiencia = 0;

    // =====================================
    // 🔥 ACREDITADO
    // =====================================

    private LocalDate fechaCertificado;

    private String evidenciaAcreditado;

    // =====================================
    // 🔥 OBSERVACIONES
    // =====================================

    @Column(length = 1000)
    private String observaciones;

    // =====================================
    // 🔥 ACTIVO
    // =====================================

    private Boolean activo = true;
}