package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;

import uno.dos.models.entity.Inscripcion;
import uno.dos.models.entity.Puesto;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.InscripcionRepository;
import uno.dos.services.TrabajadorService;
import uno.dos.services.MatrizService;
import uno.dos.services.PuestoService;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Controller
@RequestMapping("/trabajadores")
@RequiredArgsConstructor
public class TrabajadorWebController {

    private final TrabajadorService trabajadorService;
    private final TemplateEngine templateEngine;
    private final PuestoService puestoService;
    private final InscripcionRepository inscripcionRepository;
    private final MatrizService matrizService;

    @Value("${ruta.fotos}")
    private String rutaFotos;

    /* ===============================
       LISTAR TRABAJADORES
       =============================== */

    @GetMapping
    public String listar(Model model) {

        model.addAttribute("trabajadores",
                trabajadorService.listarActivos());

        return "trabajadores/lista";
    }

    /* ===============================
       FORMULARIO NUEVO
       =============================== */

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("trabajador", new Trabajador());
        model.addAttribute("puestos", puestoService.listarActivos()); // 👈 IMPORTANTE

        return "trabajadores/form";
    }

    /* ===============================
       GUARDAR TRABAJADOR
       =============================== */

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Trabajador trabajador,
            @RequestParam(required = false) MultipartFile fotoArchivo
    ) throws Exception {

        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {

            BufferedImage imagenOriginal =
                    ImageIO.read(fotoArchivo.getInputStream());

            int ancho = 300;
            int alto = 300;

            BufferedImage imagenRedimensionada =
                    new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = imagenRedimensionada.createGraphics();

            g.drawImage(imagenOriginal,0,0,ancho,alto,null);
            g.dispose();

            String nombreArchivo =
                    trabajador.getNumeroPersonal() + ".jpg";

            Path ruta = Paths.get("data/trabajadores/" + nombreArchivo);

            Files.createDirectories(ruta.getParent());

            ImageIO.write(imagenRedimensionada,"jpg",ruta.toFile());

            trabajador.setFoto(nombreArchivo);
        }

        trabajador.setActivo(true);

        trabajadorService.guardar(trabajador);

        return "redirect:/trabajadores";
    }

    /* ===============================
       IMPORTAR EXCEL
       =============================== */

    @PostMapping("/importar")
    public String importarExcel(@RequestParam("archivo") MultipartFile archivo) {

        int importados = 0;
        int duplicados = 0;
        int vacios = 0;
        
        

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    vacios++;
                    continue;
                }

                String numeroPersonal = formatter.formatCellValue(row.getCell(0)).trim();
                String curp = formatter.formatCellValue(row.getCell(1)).trim();
                String nombre = formatter.formatCellValue(row.getCell(2)).trim();
                String primerApellido = formatter.formatCellValue(row.getCell(3)).trim();
                String segundoApellido = formatter.formatCellValue(row.getCell(4)).trim();
                String areaEmpleado = formatter.formatCellValue(row.getCell(5)).trim();
                String puesto = formatter.formatCellValue(row.getCell(6)).trim();
                String claveEstado = formatter.formatCellValue(row.getCell(7)).trim();
                String claveMunicipio = formatter.formatCellValue(row.getCell(8)).trim();
                String claveOcupacion = formatter.formatCellValue(row.getCell(9)).trim();
                String claveNivelEstudios = formatter.formatCellValue(row.getCell(10)).trim();
                String claveDocProbatorio = formatter.formatCellValue(row.getCell(11)).trim();
                String email = formatter.formatCellValue(row.getCell(12)).trim();

                if (curp.isEmpty() || nombre.isEmpty()) {
                    vacios++;
                    continue;
                }

                if (curp.length() > 18) {
                    curp = curp.substring(0, 18);
                }

                if (trabajadorService.existeCurp(curp)) {
                    duplicados++;
                    continue;
                }

                Trabajador trabajador = new Trabajador();
                
                

                trabajador.setNumeroPersonal(numeroPersonal);
                trabajador.setCurp(curp);
                trabajador.setNombre(nombre);
                trabajador.setPrimerApellido(primerApellido);
                trabajador.setSegundoApellido(segundoApellido);
                trabajador.setAreaEmpleado(areaEmpleado);

                // buscar puesto
                Puesto puestoObj = puestoService.buscarPorNombre(puesto);
                trabajador.setPuesto(puestoObj);

                trabajador.setClaveEstado(claveEstado);
                trabajador.setClaveMunicipio(claveMunicipio);
                trabajador.setClaveOcupacion(claveOcupacion);
                trabajador.setClaveNivelEstudios(claveNivelEstudios);
                trabajador.setClaveDocumentoProbatorio(claveDocProbatorio);
                trabajador.setEmail(email);
                trabajador.setActivo(true);
                trabajadorService.guardar(trabajador);

                importados++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Importados: " + importados);
        System.out.println("Duplicados: " + duplicados);
        System.out.println("Vacíos: " + vacios);

        return "redirect:/trabajadores";
    }

    /* ===============================
       EXPORTAR PDF
       =============================== */

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportarPdf() throws Exception {

        List<Trabajador> trabajadores =
                trabajadorService.listarActivos();

        Context context = new Context();
        context.setVariable("trabajadores", trabajadores);

        String html =
                templateEngine.process("trabajadores/pdf", context);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=trabajadores.pdf")
                .header("Content-Type", "application/pdf")
                .body(os.toByteArray());
    }

    /* ===============================
       VER DETALLE
       =============================== */

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model){

        Trabajador trabajador = trabajadorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));

        // 🔥 SOLO inscripciones del trabajador
        List<Inscripcion> inscripciones = inscripcionRepository.findByTrabajadorId(id);

        model.addAttribute("trabajador", trabajador);
        model.addAttribute("inscripciones", inscripciones);

        // 🔥 mapa evaluaciones
        model.addAttribute("evaluaciones", matrizService.mapaEvaluaciones());

        return "trabajadores/detalle";
    }
    /* ===============================
       MOSTRAR FOTO
       =============================== */

    @GetMapping("/foto/{numero}")
    @ResponseBody
    public ResponseEntity<byte[]> verFoto(@PathVariable String numero) {

        try {

            Path ruta = Paths.get(rutaFotos, numero + ".jpg");

            if (!Files.exists(ruta)) {
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

    /* ===============================
       EDITAR
       =============================== */

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model){

        Trabajador trabajador = trabajadorService
                .buscarPorId(id)
                .orElseThrow();

        model.addAttribute("trabajador", trabajador);
        model.addAttribute("puestos", puestoService.listarActivos()); // 👈 IMPORTANTE

        return "trabajadores/form";
    }

    /* ===============================
       ACTUALIZAR
       =============================== */

    @PostMapping("/actualizar")
    public String actualizar(
            @ModelAttribute Trabajador trabajador,
            @RequestParam(required = false) MultipartFile fotoArchivo
    ) throws Exception {

        Trabajador original = trabajadorService
                .buscarPorId(trabajador.getId())
                .orElseThrow();

        // mantener foto anterior si no sube nueva
        trabajador.setFoto(original.getFoto());

        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {

            BufferedImage imagenOriginal =
                    ImageIO.read(fotoArchivo.getInputStream());

            int ancho = 300;
            int alto = 300;

            BufferedImage imagenRedimensionada =
                    new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = imagenRedimensionada.createGraphics();
            g.drawImage(imagenOriginal, 0, 0, ancho, alto, null);
            g.dispose();

            String nombreArchivo =
                    trabajador.getNumeroPersonal() + ".jpg";

            Path ruta = Paths.get(rutaFotos, nombreArchivo);

            Files.createDirectories(ruta.getParent());

            ImageIO.write(imagenRedimensionada, "jpg", ruta.toFile());

            trabajador.setFoto(nombreArchivo);
        }

        trabajador.setActivo(true);

        trabajadorService.guardar(trabajador);

        return "redirect:/trabajadores";
    }
    /* ===============================
       ELIMINAR (BORRADO LÓGICO)
       =============================== */

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        trabajadorService.eliminar(id);

        return "redirect:/trabajadores";
    }

    /* ===============================
       VER ELIMINADOS
       =============================== */

    @GetMapping("/eliminados")
    public String eliminados(Model model){

        model.addAttribute("trabajadores",
                trabajadorService.listarEliminados());

        return "trabajadores/eliminados";
    }

    /* ===============================
       RESTAURAR
       =============================== */

    @GetMapping("/restaurar/{id}")
    public String restaurar(@PathVariable Long id){

        Trabajador trabajador = trabajadorService
                .buscarPorId(id)
                .orElseThrow();

        trabajador.setActivo(true);

        trabajadorService.guardar(trabajador);

        return "redirect:/trabajadores/eliminados";
    }
    
  

}