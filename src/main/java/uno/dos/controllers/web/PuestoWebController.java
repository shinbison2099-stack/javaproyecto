package uno.dos.controllers.web;

import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Puesto;
import uno.dos.services.PuestoService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;



@Controller
@RequestMapping("/puestos")
@RequiredArgsConstructor
public class PuestoWebController {

    private final PuestoService puestoService;
    private final TemplateEngine templateEngine;

    /* LISTAR */

    @GetMapping
    public String listar(Model model){

        model.addAttribute("puestos",
                puestoService.listarActivos());

        return "puestos/lista";
    }

    /* NUEVO */

    @GetMapping("/nuevo")
    public String nuevo(Model model){

        model.addAttribute("puesto", new Puesto());

        return "puestos/form";
    }

    /* GUARDAR */

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Puesto puesto){

        puesto.setActivo(true);

        puestoService.guardar(puesto);

        return "redirect:/puestos";
    }

    /* EDITAR */

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model){

        model.addAttribute("puesto",
                puestoService.buscarPorId(id).orElseThrow());

        return "puestos/form";
    }

    /* ELIMINAR */

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        puestoService.eliminar(id);

        return "redirect:/puestos";
    }

    /* VER ELIMINADOS */

    @GetMapping("/eliminados")
    public String eliminados(Model model){

        model.addAttribute("puestos",
                puestoService.listarEliminados());

        return "puestos/eliminados";
    }

    /* RESTAURAR */

    @GetMapping("/restaurar/{id}")
    public String restaurar(@PathVariable Long id){

        puestoService.restaurar(id);

        return "redirect:/puestos/eliminados";
    }
    
    @PostMapping("/importar")
    public String importarExcel(@RequestParam MultipartFile archivo){

        try(Workbook workbook = WorkbookFactory.create(archivo.getInputStream())){

            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter = new DataFormatter();

            for(int i=1;i<=sheet.getLastRowNum();i++){

                Row row = sheet.getRow(i);

                if(row == null) continue;

                String nombre = formatter.formatCellValue(row.getCell(0));

                String nivelTexto = formatter.formatCellValue(row.getCell(1));

                Integer nivel = 0;

                if(!nivelTexto.isEmpty()){
                    nivel = Integer.parseInt(nivelTexto);
                }

                if(puestoService.existeNombre(nombre))
                    continue;

                Puesto p = new Puesto();

                p.setNombrePuesto(nombre);
                p.setNivel(nivel);
                p.setActivo(true);

                puestoService.guardar(p);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return "redirect:/puestos";
    }
    
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportarPDF() throws IOException{

        List<Puesto> puestos = puestoService.listarActivos();

        Context context = new Context();
        context.setVariable("puestos", puestos);

        String html = templateEngine.process("puestos/pdf", context);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition",
                        "attachment; filename=puestos.pdf")
                .body(os.toByteArray());
    }
    

}