package uno.dos.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Area;
import uno.dos.models.entity.SubArea;
import uno.dos.services.AreaService;
import uno.dos.services.SubAreaService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/subarea")
public class SubAreaWebController {

    private final AreaService areaService;
    private final SubAreaService subAreaService;

    // =====================================
    // VER SUBAREAS DE UN AREA
    // =====================================

    @GetMapping("/area/{id}")
    public String administrarSubareas(

            @PathVariable Long id,

            Model model){

        Area area =
                areaService
                .buscarPorId(id)
                .orElseThrow();

        model.addAttribute(
                "area",
                area
        );

        model.addAttribute(
                "subareas",
                subAreaService
                .listarPorArea(area)
        );

        return "subarea/subareas";
    }

    // =====================================
    // GUARDAR SUBAREA
    // =====================================

    @PostMapping("/guardar")
    public String guardarSubArea(

            @RequestParam Long areaId,

            @RequestParam String nombreSubArea){

        Area area =

                areaService
                .buscarPorId(areaId)
                .orElseThrow();

        SubArea subArea =
                new SubArea();

        subArea.setNombreSubArea(
                nombreSubArea
        );

        subArea.setArea(
                area
        );

        subArea.setActivo(
                true
        );

        System.out.println(
                "DESACTIVANDO: "
                + subArea.getNombreSubArea()
        );
        
        subAreaService.guardar(
                subArea
        );

        return "redirect:/subarea/area/"
                + areaId;
    }

    // =====================================
    // EDITAR
    // =====================================

    @GetMapping("/editar/{id}")
    public String editarSubArea(

            @PathVariable Long id,

            Model model){

        SubArea subArea =

                subAreaService
                .buscarPorId(id)
                .orElseThrow();

        model.addAttribute(
                "subArea",
                subArea
        );

        return "subarea/editar-subarea";
    }

    // =====================================
    // ACTUALIZAR
    // =====================================

    @PostMapping("/actualizar")
    public String actualizarSubArea(

            @RequestParam Long id,

            @RequestParam String nombreSubArea){

        SubArea subArea =

                subAreaService
                .buscarPorId(id)
                .orElseThrow();

        subArea.setNombreSubArea(
                nombreSubArea
        );

        subAreaService.guardar(
                subArea
        );

        return "redirect:/subarea/area/"
                +
                subArea
                .getArea()
                .getId();
    }

    // =====================================
    // ELIMINAR
    // =====================================

    @GetMapping("/eliminar/{id}")
    public String eliminarSubArea(

            @PathVariable Long id){

        SubArea subArea =

                subAreaService
                .buscarPorId(id)
                .orElseThrow();

        Long areaId =
                subArea
                .getArea()
                .getId();

        subAreaService.eliminar(
                id
        );

        return "redirect:/subarea/area/"
                + areaId;
    }
}