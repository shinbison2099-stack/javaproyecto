package uno.dos.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Tema;
import uno.dos.services.CursoService;
import uno.dos.services.TemaService;

@Controller
@RequestMapping("/temas")
@RequiredArgsConstructor
public class TemaWebController {

    private final TemaService temaService;
    private final CursoService cursoService;

    // 🔥 ver planificador
    @GetMapping("/curso/{cursoId}")
    public String ver(@PathVariable Long cursoId, Model model){

        model.addAttribute("curso", cursoService.buscarPorId(cursoId).orElseThrow());
        model.addAttribute("temas", temaService.listarPorCurso(cursoId));
        model.addAttribute("usadas", temaService.horasUsadas(cursoId));
        model.addAttribute("restantes", temaService.horasRestantes(cursoId));

        model.addAttribute("tema", new Tema());

        return "temas/planificador";
    }

    // 🔥 guardar tema
    @PostMapping("/guardar")
    public String guardar(Tema tema, Long cursoId){

        Curso curso = cursoService.buscarPorId(cursoId).orElseThrow();

        tema.setCurso(curso);

        temaService.guardar(tema);

        return "redirect:/temas/curso/" + cursoId;
    }
}