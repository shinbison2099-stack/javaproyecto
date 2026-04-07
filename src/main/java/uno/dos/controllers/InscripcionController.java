package uno.dos.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Curso;
import uno.dos.services.CapacitacionService;
import uno.dos.services.CursoService;
import uno.dos.services.InscripcionService;

@Controller
@RequiredArgsConstructor
public class InscripcionController {

    private final CursoService cursoService;
    private final CapacitacionService capacitacionService;
    private final InscripcionService inscripcionService;

    @GetMapping("/inscripcion")
    public String inscripcion(Model model){

        model.addAttribute("cursos", cursoService.listarActivos());
        model.addAttribute("capacitaciones", capacitacionService.listarActivas());

        return "inscripcion"; // 🔥 coincide con el HTML
    }

    @GetMapping("/inscripcion/asignar-multiple/{cursoId}/{capIds}")
    public String asignarMultiple(@PathVariable Long cursoId,
                                 @PathVariable String capIds){

        Curso curso = cursoService.buscarPorId(cursoId).orElseThrow();

        String[] ids = capIds.split(",");

        for(String idStr : ids){

            Long capId = Long.parseLong(idStr);

            Capacitacion cap = capacitacionService
                    .buscarPorId(capId)
                    .orElseThrow();

            // 🔥 EVITAR DUPLICADOS
            if(cap.getCurso() == null){

                cap.setCurso(curso);

                // 🔥 ESTA ES LA LÍNEA QUE TE FALTABA
                curso.getCapacitaciones().add(cap);

                capacitacionService.guardar(cap);
            }
        }

        return "redirect:/inscripcion";
    }
    
    @GetMapping("/inscripcion/eliminar/{id}")
    public String eliminarCapacitacion(@PathVariable Long id){

        Capacitacion cap = capacitacionService.buscarPorId(id).orElseThrow();

        cap.setCurso(null); // 🔥 DESASIGNAR

        capacitacionService.guardar(cap);

        return "redirect:/cursos/asignados";
    }
    
    @GetMapping("/flujo/{trabajadorId}")
    public String flujo(@PathVariable Long trabajadorId, Model model){

        CapacitacionTrabajador ct = inscripcionService.siguiente(trabajadorId);

        model.addAttribute("actual", ct);

        return "inscripcion/flujo";
    }
    
    @PostMapping("/calificar")
    public String calificar(Long trabajadorId, Long capId, double calificacion){

        inscripcionService.calificar(trabajadorId, capId, calificacion);

        return "redirect:/inscripcion/flujo/" + trabajadorId;
    }
}