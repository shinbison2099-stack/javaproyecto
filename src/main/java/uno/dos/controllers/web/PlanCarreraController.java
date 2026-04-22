package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uno.dos.models.entity.PlanCarrera;
import uno.dos.models.entity.PlanNivel;
import uno.dos.models.entity.Puesto;
import uno.dos.services.PlanCarreraService;
import uno.dos.services.PuestoService;

import java.util.*;

@Controller
@RequestMapping("/plan-carrera")
@RequiredArgsConstructor
public class PlanCarreraController {

    private final PlanCarreraService service;
    private final PuestoService puestoService;

    // 🔥 CREAR
    @GetMapping("/crear")
    public String crear(Model model){

        // 🔥 IMPORTANTE: mandar DTO simple para JS
        model.addAttribute("puestos",
                puestoService.listarActivos().stream()
                        .map(p -> Map.of(
                                "id", p.getId(),
                                "nombre", p.getNombrePuesto()
                        ))
                        .toList()
        );

        return "plan-carrera/crear";
    }

    // 🔥 GUARDAR
    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre,
                          @RequestParam Map<String, String> niveles){

        PlanCarrera plan = new PlanCarrera();
        plan.setNombre(nombre);

        List<PlanNivel> lista = new ArrayList<>();

        int orden = 1;

        // 🔥 ORDENAR claves (niveles[0], niveles[1], etc.)
        List<String> keysOrdenadas = new ArrayList<>(niveles.keySet());
        Collections.sort(keysOrdenadas);

        for(String key : keysOrdenadas){

            String valor = niveles.get(key);

            // 🔥 VALIDACIÓN (evita "test", "", null, etc.)
            if(valor == null || !valor.matches("\\d+")){
                continue;
            }

            Long puestoId = Long.parseLong(valor);

            Puesto puesto = puestoService.buscarPorId(puestoId)
                    .orElseThrow(() -> new RuntimeException("Puesto no encontrado: " + puestoId));

            PlanNivel n = new PlanNivel();

            // 🔥 OPCIONAL (puedes quitarlo si quieres)
            n.setNombre(puesto.getNombrePuesto());

            n.setOrden(orden++);
            n.setPlan(plan);
            n.setPuesto(puesto);

            lista.add(n);
        }

        plan.setNiveles(lista);

        service.guardar(plan);

        return "redirect:/plan-carrera/crear";
    }
}