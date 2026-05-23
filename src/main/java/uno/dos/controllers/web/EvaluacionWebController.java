package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.models.entity.NivelSkill;
import uno.dos.models.entity.SkillMatrix;
import uno.dos.services.TrabajadorService;
import uno.dos.services.CapacitacionService;
import uno.dos.services.CapacitacionTrabajadorService;
import uno.dos.services.CursoService;
import uno.dos.services.EvaluacionService;
import uno.dos.services.SkillMatrixService;

@Controller
@RequestMapping("/evaluaciones")
@RequiredArgsConstructor
public class EvaluacionWebController {

    private final TrabajadorService trabajadorService;
    private final CapacitacionService capacitacionService;
    private final EvaluacionService evaluacionService;
    private final CursoService cursoService;
    private final CapacitacionTrabajadorService capacitacionTrabajadorService;
    private final SkillMatrixService skillMatrixService;
    

    @PostMapping("/calificar")
    public String calificar(
            @RequestParam Long trabajadorId,
            @RequestParam Long capacitacionId,
            @RequestParam Integer calificacion){

        // 🔍 BUSCAR SI YA EXISTE
        EvaluacionCapacitacion ev = evaluacionService
                .buscarPorTrabajadorYCapacitacion(trabajadorId, capacitacionId);

        // 🆕 SI NO EXISTE, CREAR
        if(ev == null){
            ev = new EvaluacionCapacitacion();

            ev.setTrabajador(
                    trabajadorService.buscarPorId(trabajadorId).orElseThrow());

            ev.setCapacitacion(
                    capacitacionService.buscarPorId(capacitacionId).orElseThrow());
        }

        // 🔄 ACTUALIZAR DATOS
        ev.setCalificacion(calificacion);
        ev.setAprobada(calificacion >= 8);
        ev.setFechaEvaluacion(LocalDate.now());

        evaluacionService.guardar(ev);
        
        // 🔥 🔥 🔥 AQUÍ ESTÁ LA MAGIA 🔥 🔥 🔥
        
        return "redirect:/capacitaciones/" + capacitacionId;
    }
    
    @GetMapping("/editar")
    public String editarEvaluacion(
            @RequestParam Long trabajadorId,
            @RequestParam Long capacitacionId,
            Model model){

        EvaluacionCapacitacion ev =
                evaluacionService
                .buscarPorTrabajadorYCapacitacion(trabajadorId, capacitacionId);

        model.addAttribute("evaluacion", ev);

        return "evaluaciones/editar";
    }
    

        @GetMapping("/cursos")
        public String verCursos(Model model){

            model.addAttribute("cursos", cursoService.listarActivos());

            return "evaluaciones/cursos";
        }

        // 🔥 PASO 2: VER CAPACITACIONES DEL CURSO
        @GetMapping("/curso/{id}")
        public String verCapacitaciones(@PathVariable Long id, Model model){

            Curso curso = cursoService.buscarPorId(id).orElseThrow();

            model.addAttribute("curso", curso);
            model.addAttribute("capacitaciones", curso.getCapacitaciones());

            return "evaluaciones/capacitaciones";
        }

        // 🔥 PASO 3: IR A TU PANTALLA YA EXISTENTE
        @GetMapping("/capacitacion/{id}")
        public String verDetalle(@PathVariable Long id){

            // 👉 reutiliza tu controlador actual
            return "redirect:/capacitaciones/" + id;
        }
    
        @GetMapping("/hourly/{id}")
        public String skillMatrix(
                @PathVariable Long id,
                Model model){

            Capacitacion capacitacion =
                    capacitacionService
                    .buscarPorId(id)
                    .orElseThrow();

            List<SkillMatrix> skills =
                    skillMatrixService
                    .buscarPorCapacitacion(id);

            model.addAttribute(
                    "capacitacion",
                    capacitacion
            );

            model.addAttribute(
                    "skills",
                    skills
            );

            return "evaluaciones/skill-matrix";
        }
        
        @GetMapping("/skill/edit/{id}")
        public String editarSkill(

                @PathVariable Long id,

                Model model){

            SkillMatrix skill =
                    skillMatrixService
                    .buscarPorId(id)
                    .orElseThrow();

            model.addAttribute(
                    "skill",
                    skill
            );

            return "skill/skill-edit";
        }
        
        @PostMapping("/skill/update")
        public String actualizarSkill(

                @RequestParam Long id,

                @RequestParam NivelSkill nivel,

                @RequestParam(required = false)
                LocalDate fechaProgramada,

                @RequestParam(required = false)
                LocalDate fechaCapacitado,

                @RequestParam(required = false)
                LocalDate fechaExperiencia,

                @RequestParam(required = false)
                LocalDate fechaCertificado,

                @RequestParam(required = false)
                Integer horasExperiencia,

                @RequestParam(required = false)
                String observaciones,

                @RequestParam(required = false)
                MultipartFile archivo

        ){

            SkillMatrix skill =
                    skillMatrixService
                    .buscarPorId(id)
                    .orElseThrow();

            // =====================================
            // 🔥 ACTUALIZAR
            // =====================================

            skill.setNivel(nivel);

            skill.setFechaProgramada(
                    fechaProgramada
            );

            skill.setFechaCapacitado(
                    fechaCapacitado
            );

            skill.setFechaExperiencia(
                    fechaExperiencia
            );

            skill.setFechaCertificado(
                    fechaCertificado
            );

            skill.setHorasExperiencia(
                    horasExperiencia
            );

            skill.setObservaciones(
                    observaciones
            );

            // =====================================
            // 🔥 PDF
            // =====================================

            if(archivo != null &&
               !archivo.isEmpty()){

                try{

                    String nombreArchivo =
                            System.currentTimeMillis()
                            +
                            "_"
                            +
                            archivo.getOriginalFilename();

                    Path ruta =
                            Paths.get(
                            "uploads"
                            );

                    if(!Files.exists(ruta)){

                        Files.createDirectories(
                                ruta
                        );
                    }

                    Files.copy(

                            archivo.getInputStream(),

                            ruta.resolve(
                                    nombreArchivo
                            ),

                            StandardCopyOption
                            .REPLACE_EXISTING
                    );

                    skill.setEvidencia(
                            nombreArchivo
                    );

                }catch(Exception e){

                    e.printStackTrace();
                }
            }

            // =====================================
            // 🔥 GUARDAR
            // =====================================

            skillMatrixService.guardar(skill);

            return "redirect:/evaluaciones/hourly/"
                    +
                    skill.getCapacitacion().getId();
        }
    
}