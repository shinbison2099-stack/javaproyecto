package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.*;
import uno.dos.services.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    /* =========================
       CREAR MATRIZ
    ========================== */
    @GetMapping("/crear-salary")
    public String crear(Model model){

        // 🔥 SOLO SALARY
        model.addAttribute(
                "trabajadores",
                trabajadorService.filtrarPorTipo(
                        TipoTrabajador.SALARY
                )
        );

        // 🔥 SOLO CURSOS SALARY
        model.addAttribute(
                "cursos",
                cursoService.filtrarExacto(
                        TipoTrabajador.SALARY
                )
        );

        return "/matriz/crear";
    }

    /* =========================
       GUARDAR
    ========================== */
    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre,
                          @RequestParam(required = false) List<Long> trabajadoresIds,
                          @RequestParam(required = false) List<Long> capacitacionesIds){

        Matriz matriz = new Matriz();

        matriz.setNombre(nombre);

        if(trabajadoresIds != null){
            matriz.setTrabajadores(trabajadorService.buscarPorIds(trabajadoresIds));
        }

        if(capacitacionesIds != null){
            matriz.setCapacitaciones(capacitacionService.buscarPorIds(capacitacionesIds));
        }

        matrizService.guardar(matriz);

        return "redirect:/matriz/lista";
    }

    /* =========================
       LISTA
    ========================== */
    @GetMapping("/lista")
    public String lista(Model model){

        model.addAttribute("matrices", matrizService.listar());

        return "/matriz/lista";
    }

    /* =========================
       VER MATRIZ (🔥 AQUÍ METIMOS DNC)
    ========================== */
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model){

        Matriz matriz = matrizService.buscarPorId(id).orElseThrow();

        List<Capacitacion> capacitaciones = matriz.getCapacitaciones();

        // 🔥 SOLO TRABAJADORES INSCRITOS
        Set<Trabajador> trabajadoresSet = new HashSet<>();

        for (Capacitacion c : capacitaciones) {
            if(c.getCapacitacionTrabajadores() != null){
                for (CapacitacionTrabajador ct : c.getCapacitacionTrabajadores()) {
                    trabajadoresSet.add(ct.getTrabajador());
                }
            }
        }

        List<Trabajador> trabajadores = new ArrayList<>(trabajadoresSet);

        // 🔥 AGRUPAR POR CURSO
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

        // 🔥 EVALUACIONES
        List<EvaluacionCapacitacion> evaluaciones =
                evaluacionService.buscarPorCapacitaciones(capsIds);

        Map<String, EvaluacionCapacitacion> evalMap = new HashMap<>();

        for(EvaluacionCapacitacion ev : evaluaciones){
            String key = ev.getTrabajador().getId() + "-" + ev.getCapacitacion().getId();
            evalMap.put(key, ev);
        }

        // 🔥 MATRIZ ESTADOS
        Map<Long, Map<Long, String>> matrizEstados = new HashMap<>();

        for (Trabajador t : trabajadores) {

            Map<Long, String> capsEstado = new HashMap<>();

            for (Capacitacion c : capacitaciones) {

                String estado = "vacio";
                String key = t.getId() + "-" + c.getId();

                EvaluacionCapacitacion ev = evalMap.get(key);

                if (ev != null) {
                    estado = Boolean.TRUE.equals(ev.getAprobada()) ? "completo" : "medio";
                } else {
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

        // 🔥🔥🔥 TOTAL POR TRABAJADOR (LA CLAVE)
        Map<Long, Integer> totalPorTrabajador = new HashMap<>();

        for(Trabajador t : trabajadores){

            int totalCaps = capacitaciones.size();
            int completadas = 0;

            Map<Long, String> capsEstado = matrizEstados.get(t.getId());

            if(capsEstado != null){
                for(String estado : capsEstado.values()){
                    if("completo".equals(estado)){
                        completadas++;
                    }
                }
            }

            int porcentaje = totalCaps > 0
                    ? (completadas * 100) / totalCaps
                    : 0;

            totalPorTrabajador.put(t.getId(), porcentaje);
        }

        // 🔥 ENVIAR A VISTA
        model.addAttribute("trabajadores", trabajadores);
        model.addAttribute("capacitaciones", capacitaciones);
        model.addAttribute("capsPorCurso", capsPorCurso);
        model.addAttribute("matrizEstados", matrizEstados);
        model.addAttribute("totalPorTrabajador", totalPorTrabajador); // 🔥 NUEVO
        model.addAttribute("nombreMatriz", matriz.getNombre());

        return "/matriz/ver";
    }

    /* =========================
       CAPACITACIONES POR CURSO
    ========================== */
    @GetMapping("/capacitaciones/{cursoId}")
    @ResponseBody
    public List<Capacitacion> obtenerCapacitaciones(@PathVariable Long cursoId){
        return capacitacionService.buscarPorCursoId(cursoId);
    }

    /* =========================
       TRABAJADORES POR CURSO
    ========================== */
    @GetMapping("/trabajadores/{cursoId}")
    @ResponseBody
    public List<Trabajador> trabajadoresPorCurso(@PathVariable Long cursoId){

        List<Capacitacion> caps = capacitacionService.buscarPorCursoId(cursoId);

        return caps.stream()
                .flatMap(cap -> cap.getCapacitacionTrabajadores().stream())
                .map(CapacitacionTrabajador::getTrabajador)
                .distinct()
                .toList();
    }

    /* =========================
       FOTO
    ========================== */
    @GetMapping("/foto/{numero}")
    @ResponseBody
    public ResponseEntity<byte[]> verFoto(@PathVariable String numero) {

        try {
            String basePath = System.getProperty("user.dir");
            Path ruta = Paths.get(basePath, rutaFotos, numero + ".jpg");

            if (!Files.exists(ruta)) {
                return ResponseEntity.notFound().build();
            }

            byte[] imagen = Files.readAllBytes(ruta);

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imagen);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/matriz-data")
    @ResponseBody
    public Map<String, Object> matrizData(@RequestParam List<Long> cursoIds){

        Map<String, Object> data = new HashMap<>();

        // 🔴 VALIDACIÓN
        if(cursoIds == null || cursoIds.isEmpty()){

            data.put("capacitaciones", new ArrayList<>());
            data.put("trabajadores", new ArrayList<>());
            data.put("evaluaciones", new ArrayList<>());

            return data;
        }

        // 🔥 CAPACITACIONES
        List<Capacitacion> caps = capacitacionService.buscarPorCursoIds(cursoIds);

        // =========================================
        // 🔥 CAPACITACIONES DTO
        // =========================================

        List<Map<String, Object>> capsDTO = caps.stream().map(c -> {

            Map<String, Object> map = new HashMap<>();

            map.put("id", c.getId());
            map.put("nombreCapacitacion", c.getNombreCapacitacion());

            if(c.getCurso() != null){

                Map<String, Object> curso = new HashMap<>();
                curso.put("id", c.getCurso().getId());
                curso.put("nombreCurso", c.getCurso().getNombreCurso());

                map.put("curso", curso);

            } else {

                map.put("curso", null);
            }

            return map;

        }).collect(Collectors.toList());

        // =========================================
        // 🔥 TRABAJADORES
        // =========================================

        Set<Trabajador> trabajadoresSet = new LinkedHashSet<>();

        for(Capacitacion c : caps){

            if(c.getCapacitacionTrabajadores() != null){

                for(CapacitacionTrabajador ct : c.getCapacitacionTrabajadores()){

                    if(ct.getTrabajador() != null){
                        trabajadoresSet.add(ct.getTrabajador());
                    }
                }
            }
        }

        // =========================================
        // 🔥 TRABAJADORES DTO
        // =========================================

        List<Map<String, Object>> trabajadoresDTO = trabajadoresSet.stream().map(t -> {

            Map<String, Object> map = new HashMap<>();

            map.put("id", t.getId());
            map.put("nombre", t.getNombre());
            map.put("primerApellido", t.getPrimerApellido());
            map.put("segundoApellido", t.getSegundoApellido());
            map.put("numeroPersonal", t.getNumeroPersonal());

            if(t.getPuesto() != null){

                Map<String, Object> puesto = new HashMap<>();
                puesto.put("nombrePuesto", t.getPuesto().getNombrePuesto());

                map.put("puesto", puesto);

            } else {

                map.put("puesto", null);
            }

            return map;

        }).collect(Collectors.toList());

        // =========================================
        // 🔥 EVALUACIONES
        // =========================================

        List<EvaluacionCapacitacion> evals =
                evaluacionService.buscarPorCapacitaciones(
                        caps.stream()
                                .map(Capacitacion::getId)
                                .collect(Collectors.toList())
                );

        List<Map<String, Object>> evalDTO = evals.stream()

                .filter(e ->
                        e.getTrabajador() != null &&
                        e.getCapacitacion() != null
                )

                .map(e -> {

                    Map<String, Object> trabajador = new HashMap<>();
                    trabajador.put("id", e.getTrabajador().getId());

                    Map<String, Object> capacitacion = new HashMap<>();
                    capacitacion.put("id", e.getCapacitacion().getId());

                    Map<String, Object> map = new HashMap<>();
                    map.put("trabajador", trabajador);
                    map.put("capacitacion", capacitacion);
                    map.put("aprobada", e.getAprobada());

                    return map;

                }).collect(Collectors.toList());

        // =========================================
        // 🔥 RESPUESTA
        // =========================================

        data.put("capacitaciones", capsDTO);
        data.put("trabajadores", trabajadoresDTO);
        data.put("evaluaciones", evalDTO);

        return data;
    }
    /* =========================
       ELIMINAR
    ========================== */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {

        matrizService.eliminar(id);

        return "redirect:/matriz/lista";
    }
    
    @GetMapping("/nueva")
    public String nuevaMatriz(
            @RequestParam(defaultValue = "AMBOS") TipoTrabajador tipo,
            Model model){

        List<Curso> cursos = cursoService.filtrarPorTipo(tipo);
        List<Trabajador> trabajadores = trabajadorService.filtrarPorTipo(tipo);

        model.addAttribute("cursos", cursos);
        model.addAttribute("trabajadores", trabajadores);

        return "matriz/nueva-matriz";
    }
    
    @GetMapping("/hourly")
    public String cursosHourly(Model model){

        model.addAttribute(

                "cursos",

                cursoService.filtrarExacto(
                        TipoTrabajador.HOURLY
                )
        );

        return "matriz/hourly-cursos";
    }
    
    @GetMapping("/hourly/curso/{id}")
    public String matrizCursoHourly(

            @PathVariable Long id,

            Model model){

        Curso curso =

                cursoService
                .buscarPorId(id)
                .orElseThrow();

        List<Capacitacion> capacitaciones =

                capacitacionService
                .buscarPorCursoId(id);

        List<SkillMatrix> skills =

                matrizService
                .listarSkills();

        // =====================================
        // 🔥 SOLO INSCRITOS EN EL CURSO
        // =====================================

        Set<Trabajador> trabajadores =

                new LinkedHashSet<>();

        // =====================================
        // 🔥 MATRIZ
        // =====================================

        Map<String, SkillMatrix> matrix =
                new HashMap<>();

        for(SkillMatrix s : skills){

            matrix.put(

                    s.getTrabajador().getId()

                    +

                    "-"

                    +

                    s.getCapacitacion().getId(),

                    s
            );

            // =====================================
            // 🔥 TRABAJADORES DEL CURSO
            // =====================================

            if(

                s.getCapacitacion() != null

                &&

                s.getCapacitacion().getCurso() != null

                &&

                s.getCapacitacion()
                 .getCurso()
                 .getId()
                 .equals(id)

            ){

                trabajadores.add(
                        s.getTrabajador()
                );
            }
        }

        model.addAttribute(
                "curso",
                curso
        );

        model.addAttribute(
                "trabajadores",
                new ArrayList<>(trabajadores)
        );

        model.addAttribute(
                "capacitaciones",
                capacitaciones
        );

        model.addAttribute(
                "matrix",
                matrix
        );

        return "matriz/hourly-matrix";
    }
}