package uno.dos.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uno.dos.models.entity.Inscripcion;
import uno.dos.services.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionRestController {

    private final InscripcionService inscripcionService;

    @PostMapping
    public Inscripcion inscribir(@RequestParam Long trabajadorId,
                                 @RequestParam Long cursoId) {
        return inscripcionService.inscribir(trabajadorId, cursoId);
    }
}