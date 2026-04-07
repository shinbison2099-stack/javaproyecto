package uno.dos.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uno.dos.models.entity.Asistencia;
import uno.dos.services.AsistenciaService;

@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
public class AsistenciaRestController {

    private final AsistenciaService asistenciaService;

    @PostMapping("/registrar")
    public Asistencia registrar(@RequestParam Long trabajadorId,
                                @RequestParam Long cursoId,
                                @RequestParam Long dispositivoId) {

        return asistenciaService
                .registrarAsistencia(trabajadorId, cursoId, dispositivoId);
    }
}