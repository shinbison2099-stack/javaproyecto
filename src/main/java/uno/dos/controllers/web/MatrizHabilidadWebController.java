package uno.dos.controllers.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uno.dos.dto.MatrizHourlyDTO;
import lombok.RequiredArgsConstructor;
import uno.dos.dto.CursoDTO;
import uno.dos.models.entity.Area;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Habilidad;
import uno.dos.models.entity.MatrizHabilidad;
import uno.dos.models.entity.SubArea;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.models.entity.Trabajador;
import uno.dos.services.AreaService;
import uno.dos.services.CursoService;
import uno.dos.services.HabilidadService;
import uno.dos.services.MatrizHabilidadService;
import uno.dos.services.MatrizHourlyService;
import uno.dos.services.SubAreaService;
import uno.dos.services.TrabajadorService;
import uno.dos.services.MatrizHourlyService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/matriz-habilidad")
public class MatrizHabilidadWebController {

    private final MatrizHabilidadService matrizService;

    private final AreaService areaService;

    private final SubAreaService subAreaService;

    private final HabilidadService habilidadService;

    private final CursoService cursoService;
    
    private final TrabajadorService trabajadorService;
    
    private final MatrizHourlyService matrizHourlyService;
    
        
    @GetMapping
    public String inicio(){

        return "redirect:/matriz-habilidad/lista";
    } 

    @GetMapping("/lista")
    public String listar(Model model){

        model.addAttribute(
                "matrices",
                matrizService.listar()
        );

        return "matriz-habilidad/lista";
    }
    
    

    @GetMapping("/nuevo")
    public String nuevo(Model model){

        model.addAttribute(
                "matriz",
                new MatrizHabilidad()
        );

        model.addAttribute(
                "areas",
                areaService.listar()
        );

        return "matriz-habilidad/wizard";
    }
    
    @PostMapping("/guardar")
    public String guardar(

            @RequestParam String nombre,

            @RequestParam Long areaId,

            @RequestParam Long subAreaId,

            @RequestParam TipoTrabajador tipoTrabajador,

            @RequestParam(required = false)
            List<Long> habilidadIds,

            @RequestParam(required = false)
            List<Long> cursoIds,

            @RequestParam(required = false)
            List<Long> trabajadorIds){

        matrizService.guardar(
                nombre,
                areaId,
                subAreaId,
                tipoTrabajador,
                habilidadIds,
                cursoIds,
                trabajadorIds
        );
        System.out.println(
                "HABILIDADES: " +
                habilidadIds
        );
        return "redirect:/matriz-habilidad";
    }
    
    
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model){

        MatrizHabilidad matriz =
                matrizService
                .buscarPorId(id)
                .orElseThrow();

        model.addAttribute(
                "matriz",
                matriz
        );

        model.addAttribute(
                "areas",
                areaService.listar()
        );

        model.addAttribute(
                "todasLasHabilidades",
                habilidadService.listar()
        );

        model.addAttribute(
                "todosLosCursos",
                cursoService.listarActivos()
        );

        model.addAttribute(
                "todosLosTrabajadores",
                trabajadorService.listarActivos()
        );

        return "matriz-habilidad/editar";
    }
    
    @PostMapping("/actualizar")
    public String actualizar(

            @RequestParam Long id,

            @RequestParam String nombre,

            @RequestParam Long areaId,

            @RequestParam Long subAreaId,

            @RequestParam TipoTrabajador tipoTrabajador,

            @RequestParam(required = false)
            List<Long> habilidadIds,

            @RequestParam(required = false)
            List<Long> cursoIds,

            @RequestParam(required = false)
            List<Long> trabajadorIds){

        matrizService.actualizar(
                id,
                nombre,
                areaId,
                subAreaId,
                tipoTrabajador,
                habilidadIds,
                cursoIds,
                trabajadorIds
        );

        return "redirect:/matriz-habilidad";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes flash){

        try {

            matrizService.eliminar(id);

            flash.addFlashAttribute(
                    "success",
                    "Matriz eliminada correctamente"
            );

        } catch (RuntimeException e) {

            flash.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/matriz-habilidad";
    }
    
    @GetMapping("/subareas/{areaId}")
    @ResponseBody
    public List<SubArea> obtenerSubareas(
            @PathVariable Long areaId){

        Area area =
                areaService
                .buscarPorId(areaId)
                .orElseThrow();

        return subAreaService
                .listarPorArea(area);
    }
    
    @GetMapping("/habilidades/{subAreaId}")
    @ResponseBody
    public List<Habilidad> obtenerHabilidades(
            @PathVariable Long subAreaId){

        return habilidadService
                .buscarPorSubArea(
                        subAreaId
                );
    }
    
    @GetMapping("/cursos/{tipo}")
    @ResponseBody
    public List<CursoDTO> obtenerCursos(
            @PathVariable TipoTrabajador tipo){

        return cursoService
                .filtrarPorTipo(tipo)
                .stream()
                .map(c -> new CursoDTO(
                        c.getId(),
                        c.getNombreCurso()
                ))
                .toList();
    }
    
    @GetMapping("/trabajadores/{tipo}")
    @ResponseBody
    public List<Trabajador> obtenerTrabajadores(
            @PathVariable TipoTrabajador tipo){

        return trabajadorService
                .buscarPorTipo(tipo);
    }
    
    @GetMapping("/ver/{id}")
    public String verMatriz(
            @PathVariable Long id,
            Model model){

        MatrizHabilidad matriz =
                matrizService
                .buscarPorId(id)
                .orElseThrow();

        if(matriz.getTipoTrabajador()
                == TipoTrabajador.HOURLY){

            MatrizHourlyDTO dto =
                    matrizHourlyService
                    .construir(id);

            model.addAttribute(
                    "matriz",
                    dto
            );

            return "matriz-habilidad/ver-hourly";
        }

        model.addAttribute(
                "matriz",
                matriz
        );

        return "matriz-habilidad/ver-salary";
    }
    
    @GetMapping("/validar-eliminar/{id}")
    @ResponseBody
    public Map<String,Object> validarEliminar(
            @PathVariable Long id){

        MatrizHabilidad matriz =
                matrizService
                .buscarPorId(id)
                .orElseThrow();

        Map<String,Object> respuesta =
                new HashMap<>();

        respuesta.put(
                "puedeEliminar",
                matriz.getTrabajadores().isEmpty()
        );

        respuesta.put(
                "totalTrabajadores",
                matriz.getTrabajadores().size()
        );

        return respuesta;
    }
    
    
}