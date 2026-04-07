package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import uno.dos.models.entity.Curso;
import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.services.TrabajadorService;
import uno.dos.services.CapacitacionService;
import uno.dos.services.CursoService;
import uno.dos.services.EvaluacionService;

@Controller
@RequestMapping("/evaluaciones")
@RequiredArgsConstructor
public class EvaluacionWebController {

    private final TrabajadorService trabajadorService;
    private final CapacitacionService capacitacionService;
    private final EvaluacionService evaluacionService;
    private final CursoService cursoService;
    

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
    
    
    
}