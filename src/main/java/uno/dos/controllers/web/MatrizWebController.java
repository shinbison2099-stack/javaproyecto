package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        matriz.setNombre(nombre); // 🔥 IMPORTANTE

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

        List<Trabajador> trabajadores = matriz.getTrabajadores();
        List<Capacitacion> capacitaciones = matriz.getCapacitaciones();
        
        Map<String, List<Capacitacion>> capsPorCurso = new LinkedHashMap<>();

        for (Capacitacion c : capacitaciones) {

            if(c.getCurso() == null) continue;

            String nombreCurso = c.getCurso().getNombreCurso();

            capsPorCurso
                .computeIfAbsent(nombreCurso, k -> new ArrayList<>())
                .add(c);
        }

        // 🔥 eliminar cursos vacíos
        capsPorCurso.entrySet().removeIf(e -> e.getValue().isEmpty());

        model.addAttribute("capsPorCurso", capsPorCurso);

        // 🔥 MAPA DE ESTADOS
        Map<Long, Map<Long, String>> matrizEstados = new HashMap<>();

        for (Trabajador t : trabajadores) {

            Map<Long, String> capsEstado = new HashMap<>();

            for (Capacitacion c : capacitaciones) {

                String estado = "vacio";

                for (CapacitacionTrabajador ct : c.getCapacitacionTrabajadores()) {

                    if (ct.getTrabajador().getId().equals(t.getId())) {

                        if (Boolean.TRUE.equals(ct.getAprobado())) {
                            estado = "completo";
                        } else if (ct.getNivel() != null) {
                            estado = "medio";
                        }

                        break;
                    }
                }

                capsEstado.put(c.getId(), estado);
            }

            matrizEstados.put(t.getId(), capsEstado);
        }

        model.addAttribute("trabajadores", trabajadores);
        model.addAttribute("capacitaciones", capacitaciones);
        model.addAttribute("matrizEstados", matrizEstados); // 🔥 IMPORTANTE

        return "matriz/ver";
    }
    
    @GetMapping("/capacitaciones/{cursoId}")
    @ResponseBody
    public List<Capacitacion> obtenerCapacitaciones(@PathVariable Long cursoId){
    	List<Capacitacion> lista = capacitacionService.buscarPorCursoId(cursoId);
        Curso curso = cursoService.buscarPorId(cursoId).orElseThrow();
        System.out.println("🔥 CAPACITACIONES: " + lista.size());
    	return capacitacionService.buscarPorCursoId(cursoId);
    }
    
    @GetMapping("/trabajadores/{cursoId}")
    @ResponseBody
    public List<Trabajador> trabajadoresPorCurso(@PathVariable Long cursoId){

        List<Capacitacion> caps = capacitacionService.buscarPorCursoId(cursoId);

        return caps.stream()
                .flatMap(cap -> cap.getCapacitacionTrabajadores().stream())
                .map(ct -> ct.getTrabajador())
                .distinct()
                .toList();
    }
    
    @GetMapping("/matriz-data")
    @ResponseBody
    public Map<String, Object> obtenerMatriz(@RequestParam List<Long> cursoIds){

        // 🔥 TRAER CAPACITACIONES
        List<Capacitacion> caps = capacitacionService.buscarPorCursoIds(cursoIds);

        // ⚠️ (OPCIONAL PERO REALMENTE NO NECESARIO)
        caps = caps.stream()
                .filter(c -> c.getCurso() != null)
                .toList();

        // 🔥 SI NO HAY CAPACITACIONES
        if(caps.isEmpty()){
            Map<String, Object> data = new HashMap<>();
            data.put("capacitaciones", caps);
            data.put("trabajadores", List.of());
            return data;
        }

        // 🔥 TRAER TRABAJADORES INSCRITOS
        List<Trabajador> trabajadores = caps.stream()
                .flatMap(c -> c.getCapacitacionTrabajadores().stream())
                .map(ct -> ct.getTrabajador())
                .distinct()
                .toList();

        // 🔥 RESPUESTA
        Map<String, Object> data = new HashMap<>();
        data.put("capacitaciones", caps);
        data.put("trabajadores", trabajadores);

        return data;
    }
    
    @GetMapping("/matriz-dinamica")
    @ResponseBody
    public Map<String, Object> obtenerMatrizDinamica(@RequestParam List<Long> cursosIds){

        List<Capacitacion> caps = capacitacionService.buscarPorCursoIds(cursosIds);

        List<Trabajador> trabajadores = caps.stream()
                .flatMap(c -> c.getCapacitacionTrabajadores().stream())
                .map(ct -> ct.getTrabajador())
                .distinct()
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("capacitaciones", caps);
        data.put("trabajadores", trabajadores);

        return data;
    }
    
}