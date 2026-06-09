package uno.dos.controllers.web;


import java.util.List;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uno.dos.models.entity.Area;
import org.apache.poi.ss.usermodel.Sheet;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.SubArea;
import uno.dos.services.AreaService;
import uno.dos.services.SubAreaService;
import uno.dos.models.entity.Area;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Controller
@RequiredArgsConstructor
public class AreaWebController {

    private final AreaService areaService;
    private final SubAreaService subAreaService;

    // =====================================
    // 🔥 LISTAR AREAS
    // =====================================

    @GetMapping("/area")
    public String listarAreas(Model model){

        List<Area> areas =
                areaService.listar();

        model.addAttribute(
                "areas",
                areas
        );

        return "area/areas";
    }
    
    @GetMapping("/area/nuevo")
    public String nuevaArea(
            Model model){

        model.addAttribute(
                "area",
                new Area()
        );

        return "area/nueva-area";
    }
    
    @PostMapping("/area/guardar")
    public String guardarArea(

            Area area,

            @RequestParam(required = false)
            String subareas){

        Area areaGuardada =
                areaService.guardar(area);

        System.out.println(
                "AREA = "
                + areaGuardada.getNombreArea()
        );

        System.out.println(
                "SUBAREAS = "
                + subareas
        );

        if(subareas != null &&
           !subareas.isBlank()){

            String[] lista =
                    subareas.split(",");

            for(String nombre : lista){

                SubArea sub =
                        new SubArea();

                sub.setNombreSubArea(
                        nombre.trim()
                );

                sub.setArea(
                        areaGuardada
                );

                sub.setActivo(
                        true
                );

                subAreaService.guardar(
                        sub
                );
            }
        }

        return "redirect:/area";
    }
    
    @GetMapping("/area/editar/{id}")
    public String editarArea(

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

        return "area/nueva-area";
    }
    
    @GetMapping("/area/eliminar/{id}")
    public String eliminarArea(

            @PathVariable Long id){

        areaService.eliminar(id);

        return "redirect:/area";
    }
    
    @PostMapping("/area/importar")
    public String importarExcel(

            @RequestParam MultipartFile archivo){

        try{

            Workbook workbook =
                    new XSSFWorkbook(
                            archivo.getInputStream()
                    );

            Sheet hoja =
                    workbook.getSheetAt(0);

            for(int i = 1;
                i <= hoja.getLastRowNum();
                i++){

                Row fila =
                        hoja.getRow(i);

                if(fila == null){
                    continue;
                }

                String nombreArea =
                        fila.getCell(0)
                        .getStringCellValue()
                        .trim();

                String subAreasTexto =
                        fila.getCell(1)
                        .getStringCellValue()
                        .trim();

                // ============================
                // BUSCAR O CREAR AREA
                // ============================

                Area area;

                List<Area> areas =
                        areaService.listar();

                area = areas.stream()

                        .filter(a ->
                                a.getNombreArea()
                                 .equalsIgnoreCase(
                                         nombreArea
                                 ))

                        .findFirst()

                        .orElse(null);

                if(area == null){

                    area = new Area();

                    area.setNombreArea(
                            nombreArea
                    );

                    area.setActivo(
                            true
                    );

                    area =
                            areaService
                            .guardar(area);
                }

                // ============================
                // SUBAREAS SEPARADAS POR COMA
                // ============================

                String[] subAreas =
                        subAreasTexto.split(",");

                for(String nombreSubArea : subAreas){

                    nombreSubArea =
                            nombreSubArea.trim();

                    if(nombreSubArea.isBlank()){
                        continue;
                    }

                    if(!subAreaService
                            .existeNombre(
                                    nombreSubArea,
                                    area
                            )){

                        SubArea sub =
                                new SubArea();

                        sub.setNombreSubArea(
                                nombreSubArea
                        );

                        sub.setArea(
                                area
                        );

                        sub.setActivo(
                                true
                        );

                        subAreaService.guardar(
                                sub
                        );
                    }
                }
            }

            workbook.close();

        }catch(Exception e){

            e.printStackTrace();
        }

        return "redirect:/area";
    }
}