package uno.dos.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uno.dos.models.entity.Trabajador;
import uno.dos.services.TrabajadorService;

import java.util.List;

@RestController
@RequestMapping("/api/trabajador")
@RequiredArgsConstructor
public class TrabajadorRestController {

    private final TrabajadorService trabajadorService;

    @GetMapping
    public List<Trabajador> listar() {
        return trabajadorService.listarActivos();
    }

    @PostMapping
    public Trabajador crear(@RequestBody Trabajador trabajador) {
        return trabajadorService.guardar(trabajador);
    }
}