package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Inscripcion;
import uno.dos.models.entity.Matriz;
import uno.dos.models.entity.Trabajador;
import uno.dos.services.CapacitacionService;
import uno.dos.services.CapacitacionTrabajadorService;
import uno.dos.services.CursoService;
import uno.dos.services.InscripcionService;
import uno.dos.services.MatrizService;
import uno.dos.services.TrabajadorService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/matriz")
@RequiredArgsConstructor
public class MatrizWebController {

    private final TrabajadorService trabajadorService;
    private final CapacitacionService capacitacionService;
    private final CapacitacionTrabajadorService capacitacionTrabajadorService;
    private final MatrizService matrizService;
    private final CursoService cursoService;
    private final InscripcionService inscripcionService;
    /**
     * 🔥 MATRIZ PRINCIPAL
     */
      
    @GetMapping("/crear")
    public String crear(Model model){

        model.addAttribute("trabajadores", trabajadorService.listarActivos());
        model.addAttribute("cursos", cursoService.listarActivos());
        
        return "/matriz/crear";
    }
    
    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre,
                          @RequestParam List<Long> trabajadoresIds,
                          @RequestParam List<Long> capacitacionesIds){

        Matriz matriz = new Matriz();

        matriz.setNombre(nombre);

        matriz.setTrabajadores(
        	    trabajadorService.buscarPorIds(trabajadoresIds)
        	);

        	matriz.setCapacitaciones(
        	    capacitacionService.buscarPorIds(capacitacionesIds)
        	);

        matrizService.guardar(matriz);

        return "redirect:/matriz/lista";
    }
    
    @GetMapping("/lista")
    public String lista(Model model){

        model.addAttribute("matrices", matrizService.listar());

        return "/matriz/lista";
    }
    
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model){

        Matriz matriz = matrizService.buscarPorId(id).orElseThrow();

        model.addAttribute("trabajadores", matriz.getTrabajadores());
        model.addAttribute("capacitaciones", matriz.getCapacitaciones());

        return "/matriz";
    }
    
    @GetMapping("/capacitaciones/{cursoId}")
    @ResponseBody
    public List<Capacitacion> obtenerCapacitaciones(@PathVariable Long cursoId){

        Curso curso = cursoService.buscarPorId(cursoId).orElseThrow();

        return curso.getCapacitaciones();
    }
    
    @GetMapping("/trabajadores/{cursoId}")
    @ResponseBody
    public List<Trabajador> trabajadoresPorCurso(@PathVariable Long cursoId){

        List<Inscripcion> inscripciones = inscripcionService.listarPorCurso(cursoId);

        return inscripciones.stream()
                .map(Inscripcion::getTrabajador)
                .toList();
    }
}