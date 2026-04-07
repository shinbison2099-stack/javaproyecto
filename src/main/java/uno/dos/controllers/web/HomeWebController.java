package uno.dos.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import uno.dos.services.CapacitacionService;
import uno.dos.services.CursoService;

@Controller
@RequiredArgsConstructor
public class HomeWebController {

    private final CursoService cursoService;
    private final CapacitacionService capacitacionService;
    @GetMapping("/")
    public String inicio(){
        return "index";
    }

    @GetMapping("/configuracion")
    public String configuracion(){
        return "configuracion";
    }

    @GetMapping("/operaciones")
    public String operaciones(Model model){

        model.addAttribute("cursos", cursoService.listarActivos());

        return "operaciones";
    }
    
    
}