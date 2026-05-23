package uno.dos.models.entity;

import java.time.LocalDate;
import java.util.List;

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
    // 🔥 NIVEL SKILL
    // =====================================

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_skill")
    private NivelSkill nivel = NivelSkill.VACIO;

    /*
        VACIO
        PROGRAMADO
        CAPACITADO
        EXPERIENCIA_40H
        CERTIFICADO
    */

    // =====================================
    // 🔥 EVIDENCIA
    // =====================================

    private String evidencia;

    // =====================================
    // 🔥 FECHAS
    // =====================================

    private LocalDate fechaProgramada;

    private LocalDate fechaCapacitado;

    private LocalDate fechaExperiencia;

    private LocalDate fechaCertificado;

    // =====================================
    // 🔥 HORAS EXPERIENCIA
    // =====================================

    @Column(name = "horas_experiencia")
    private Integer horasExperiencia = 0;

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