package uno.dos.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.CategoriaPuesto;
import uno.dos.services.CategoriaPuestoService;

@Controller
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaPuestoWebController {

    private final CategoriaPuestoService categoriaService;

    @GetMapping("/nuevo")
    public String nuevo(Model model){
        model.addAttribute("categoria", new CategoriaPuesto());
        return "categorias/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute CategoriaPuesto categoria){

        categoria.setActivo(true);

        categoriaService.guardar(categoria);

        return "redirect:/puestos";
    }
}