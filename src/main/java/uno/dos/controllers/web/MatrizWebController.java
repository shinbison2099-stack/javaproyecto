package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.models.entity.Inscripcion;
import uno.dos.models.entity.Matriz;
import uno.dos.models.entity.Trabajador;
import uno.dos.services.CapacitacionService;
import uno.dos.services.CapacitacionTrabajadorService;
import uno.dos.services.CursoService;
import uno.dos.services.EvaluacionService;
import uno.dos.services.InscripcionService;
import uno.dos.services.MatrizService;
import uno.dos.services.TrabajadorService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
    private final EvaluacionService evaluacionService;
    
    @Value("${ruta.fotos}")
    private String rutaFotos;
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

        // 🔥 AGRUPAR POR CURSO (IMPORTANTE PARA EL HTML)
        Map<String, List<Capacitacion>> capsPorCurso = new LinkedHashMap<>();

        for (Capacitacion c : capacitaciones) {

            if(c.getCurso() == null) continue;

            String nombreCurso = c.getCurso().getNombreCurso();

            capsPorCurso
                    .computeIfAbsent(nombreCurso, k -> new ArrayList<>())
                    .add(c);
        }

        // 🔥 IDS
        List<Long> capsIds = capacitaciones.stream()
                .map(Capacitacion::getId)
                .toList();

        // 🔥 TRAER EVALUACIONES
        List<EvaluacionCapacitacion> evaluaciones =
                evaluacionService.buscarPorCapacitaciones(capsIds);

        // 🔥 MAPA EVALUACIONES
        Map<String, EvaluacionCapacitacion> evalMap = new HashMap<>();

        for(EvaluacionCapacitacion ev : evaluaciones){
            String key = ev.getTrabajador().getId() + "-" + ev.getCapacitacion().getId();
            evalMap.put(key, ev);
        }

        // 🔥 MATRIZ
        Map<Long, Map<Long, String>> matrizEstados = new HashMap<>();

        for (Trabajador t : trabajadores) {

            Map<Long, String> capsEstado = new HashMap<>();

            for (Capacitacion c : capacitaciones) {

                String estado = "vacio";
                String key = t.getId() + "-" + c.getId();

                EvaluacionCapacitacion ev = evalMap.get(key);

                if (ev != null) {

                    if (Boolean.TRUE.equals(ev.getAprobada())) {
                        estado = "completo";
                    } else {
                        estado = "medio";
                    }

                } else {

                    // 🔥 MEJOR VALIDACIÓN DE INSCRIPCIÓN
                    boolean inscrito = c.getCapacitacionTrabajadores() != null &&
                            c.getCapacitacionTrabajadores().stream()
                                    .anyMatch(ct -> ct.getTrabajador().getId().equals(t.getId()));

                    if(inscrito){
                        estado = "medio";
                    }
                }

                capsEstado.put(c.getId(), estado);
            }

            matrizEstados.put(t.getId(), capsEstado);
        }

        // 🔥 ENVIAR TODO
        model.addAttribute("trabajadores", trabajadores);
        model.addAttribute("capacitaciones", capacitaciones);
        model.addAttribute("capsPorCurso", capsPorCurso); // 🔥 FALTABA
        model.addAttribute("matrizEstados", matrizEstados);
        model.addAttribute("nombreMatriz", matriz.getNombre());

        return "/matriz/ver";
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
    public Map<String, Object> obtenerDatos(@RequestParam List<Long> cursoIds){

        List<Capacitacion> caps = capacitacionService.buscarPorCursoIds(cursoIds);

        List<Long> capsIds = caps.stream()
                .map(Capacitacion::getId)
                .toList();

        // 🔥 TRAER EVALUACIONES
        List<EvaluacionCapacitacion> evaluaciones =
                evaluacionService.buscarPorCapacitaciones(capsIds);

        List<Trabajador> trabajadores = caps.stream()
                .flatMap(c -> c.getCapacitacionTrabajadores().stream())
                .map(ct -> ct.getTrabajador())
                .distinct()
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("capacitaciones", caps);
        data.put("trabajadores", trabajadores);

        // 🔥 ESTA LÍNEA TE FALTA
        data.put("evaluaciones", evaluaciones);

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
    
    @GetMapping("/foto/{numero}")
    @ResponseBody
    public ResponseEntity<byte[]> verFoto(@PathVariable String numero) {

        try {

            // 🔥 RUTA REAL DEL PROYECTO
            String basePath = System.getProperty("user.dir");

            Path ruta = Paths.get(basePath, rutaFotos, numero + ".jpg");

            System.out.println("BUSCANDO: " + ruta);

            if (!Files.exists(ruta)) {
                System.out.println("NO EXISTE");
                return ResponseEntity.notFound().build();
            }

            byte[] imagen = Files.readAllBytes(ruta);

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imagen);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {

        matrizService.eliminar(id);

        return "redirect:/matriz/lista";
    }
    
}