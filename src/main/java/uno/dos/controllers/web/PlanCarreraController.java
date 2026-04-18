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
    
    @GetMapping("/crear")
    public String crear(Model model){

        model.addAttribute("puestos", puestoService.listarActivos());

        return "plan-carrera/crear";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre,
                          @RequestParam Map<String, String> niveles){

        PlanCarrera plan = new PlanCarrera();
        plan.setNombre(nombre);

        List<PlanNivel> lista = new ArrayList<>();

        int orden = 1;

        for(String key : niveles.keySet()){

            Long puestoId = Long.parseLong(niveles.get(key));

            Puesto puesto = puestoService.buscarPorId(puestoId).orElseThrow();

            PlanNivel n = new PlanNivel();
            n.setNombre(puesto.getNombrePuesto()); // opcional
            n.setOrden(orden++);
            n.setPlan(plan);
            n.setPuesto(puesto); // 🔥 IMPORTANTE

            lista.add(n);
        }

        plan.setNiveles(lista);

        service.guardar(plan);

        return "redirect:/plan-carrera/crear";
    }
}