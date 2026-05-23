package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import uno.dos.models.entity.Rol;
import uno.dos.models.entity.TipoInstructor;
import uno.dos.models.entity.Instructores;
import uno.dos.services.InstructorService;
import uno.dos.services.util.PdfService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Controller
@RequestMapping("/instructores")
@RequiredArgsConstructor
public class InstructorWebController {

    private final InstructorService instructorService;
    private final PdfService pdfService;
    private final TemplateEngine templateEngine;

    /* ===============================
       LISTAR
    =============================== */

    @GetMapping
    public String listar(Model model) {

        model.addAttribute("instructores",
                instructorService.listarActivos());
        System.out.println("TOTAL ACTIVOS: " + instructorService.listarActivos().size());
        return "instructores/lista";
    }

    /* ===============================
       NUEVO
    =============================== */

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        Instructores instructor = new Instructores();

        // 🔥 valores por defecto (opcional pero recomendado)
        instructor.setActivo(true);
        instructor.setRol(Rol.INSTRUCTOR);

        model.addAttribute("instructor", instructor);

        return "instructores/form";
    }

    /* ===============================
       GUARDAR
    =============================== */

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Instructores instructor,
                          Model model) {

        // 🔥 VALIDAR TIPO
        if(instructor.getTipo() == null){
            model.addAttribute("error", "Selecciona tipo de instructor");
            model.addAttribute("instructor", instructor);
            return "instructores/form";
        }

        instructor.setRol(Rol.INSTRUCTOR);
        instructor.setActivo(true);

        instructorService.guardar(instructor);

        return "redirect:/instructores";
    }

    /* ===============================
       IMPORTAR EXCEL
    =============================== */

    @PostMapping("/importar")
    public String importarExcel(@RequestParam("archivo") MultipartFile archivo) {

        int importados = 0;
        int duplicados = 0;
        int errores = 0;

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                try {

                    Row row = sheet.getRow(i);

                    if (row == null) {
                        continue;
                    }

                    // =====================================
                    // 🔥 LEER CAMPOS
                    // =====================================

                    String nombre =
                            formatter.formatCellValue(row.getCell(0)).trim();

                    String email =
                            formatter.formatCellValue(row.getCell(1))
                                    .trim()
                                    .toLowerCase();

                    String password =
                            formatter.formatCellValue(row.getCell(2)).trim();

                    String tipoTexto =
                            formatter.formatCellValue(row.getCell(3)).trim();

                    // =====================================
                    // 🔥 VALIDACIONES
                    // =====================================

                    if (nombre.isBlank() || email.isBlank()) {

                        System.out.println("⚠️ Fila vacía o inválida: " + i);

                        errores++;

                        continue;
                    }

                    // =====================================
                    // 🔥 DUPLICADOS
                    // =====================================

                    if (instructorService.existeEmail(email)) {

                        System.out.println("⚠️ Email duplicado: " + email);

                        duplicados++;

                        continue;
                    }

                    // =====================================
                    // 🔥 TIPO
                    // =====================================

                    TipoInstructor tipo;

                    if (tipoTexto.equalsIgnoreCase("interno")
                            || tipoTexto.equalsIgnoreCase("i")) {

                        tipo = TipoInstructor.INTERNO;

                    } else if (tipoTexto.equalsIgnoreCase("externo")
                            || tipoTexto.equalsIgnoreCase("e")) {

                        tipo = TipoInstructor.EXTERNO;

                    } else {

                        // 🔥 DEFAULT
                        tipo = TipoInstructor.INTERNO;
                    }

                    // =====================================
                    // 🔥 PASSWORD DEFAULT
                    // =====================================

                    if (password.isBlank()) {

                        password = "12345";
                    }

                    // =====================================
                    // 🔥 CREAR INSTRUCTOR
                    // =====================================

                    Instructores instructor = new Instructores();

                    instructor.setNombre(nombre);
                    instructor.setEmail(email);
                    instructor.setPassword(password);

                    instructor.setRol(Rol.INSTRUCTOR);

                    instructor.setActivo(true);

                    // 🔥 INTERNO / EXTERNO
                    instructor.setTipo(tipo);

                    // =====================================
                    // 🔥 GUARDAR
                    // =====================================

                    instructorService.guardar(instructor);

                    importados++;

                } catch (Exception e) {

                    errores++;

                    System.out.println(
                            "❌ Error en fila "
                                    + i
                                    + ": "
                                    + e.getMessage()
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        // =====================================
        // 🔥 RESUMEN
        // =====================================

        System.out.println("✅ Importados: " + importados);
        System.out.println("⚠️ Duplicados: " + duplicados);
        System.out.println("❌ Errores: " + errores);

        return "redirect:/instructores";
    }

    /* ===============================
       EXPORTAR PDF
    =============================== */

    @GetMapping("/pdf")
    public void exportarPdf(HttpServletResponse response) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=instructores.pdf");

        List<Instructores> lista = instructorService.listarActivos();

        Context context = new Context();
        context.setVariable("instructores", lista);

        String html = templateEngine.process("instructores/pdf", context);

        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.withHtmlContent(html, null);
        builder.toStream(response.getOutputStream());
        builder.run();
    }

    /* ===============================
       EDITAR
    =============================== */

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        Instructores instructor = instructorService
                .buscarPorId(id)
                .orElseThrow(() ->
                        new RuntimeException("Instructor no encontrado"));

        model.addAttribute("instructor", instructor);

        return "instructores/form";
    }

    /* ===============================
       ACTUALIZAR
    =============================== */

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Instructores instructor,
                             Model model) {

        if (instructorService.emailExiste(
                instructor.getEmail(),
                instructor.getId())) {

            model.addAttribute("error",
                    "El email ya está registrado");

            model.addAttribute("instructor", instructor);

            return "instructores/form";
        }

        instructor.setActivo(true);

        instructorService.guardar(instructor);

        return "redirect:/instructores";
    }

    /* ===============================
       ELIMINAR
    =============================== */

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){
        instructorService.eliminar(id);
        return "redirect:/instructores";
    }
    
    @GetMapping("/eliminados")
    public String eliminados(Model model){

        model.addAttribute("instructores",
                instructorService.listarEliminados());

        return "instructores/eliminados";
    }

    @GetMapping("/restaurar/{id}")
    public String restaurar(@PathVariable Long id){

        Instructores instructor = instructorService
                .buscarPorId(id)
                .orElseThrow();

        instructor.setActivo(true);

        instructorService.guardar(instructor);

        return "redirect:/instructores/eliminados";
    }
    
    @GetMapping("/plantilla")
    public void descargarPlantilla(HttpServletResponse response) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Plantilla");

            // =====================================
            // 🔥 ESTILO HEADER
            // =====================================

            CellStyle headerStyle = workbook.createCellStyle();

            Font headerFont = workbook.createFont();

            headerFont.setBold(true);

            headerStyle.setFont(headerFont);

            // =====================================
            // 🔥 ENCABEZADOS
            // =====================================

            Row header = sheet.createRow(0);

            String[] columnas = {
                    "nombre",
                    "email",
                    "password",
                    "tipo"
            };

            for (int i = 0; i < columnas.length; i++) {

                Cell cell = header.createCell(i);

                cell.setCellValue(columnas[i]);

                cell.setCellStyle(headerStyle);
            }

            // =====================================
            // 🔥 FILA EJEMPLO
            // =====================================

            Row ejemplo = sheet.createRow(1);

            ejemplo.createCell(0)
                    .setCellValue("Juan Perez");

            ejemplo.createCell(1)
                    .setCellValue("juan@mail.com");

            ejemplo.createCell(2)
                    .setCellValue("1234");

            ejemplo.createCell(3)
                    .setCellValue("INTERNO");

            // =====================================
            // 🔥 VALIDACIÓN TIPO
            // =====================================

            DataValidationHelper helper =
                    sheet.getDataValidationHelper();

            DataValidationConstraint constraint =
                    helper.createExplicitListConstraint(
                            new String[]{
                                    "INTERNO",
                                    "EXTERNO"
                            }
                    );

            // 🔥 COLUMNA 3
            CellRangeAddressList addressList =
                    new CellRangeAddressList(
                            1,
                            1000,
                            3,
                            3
                    );

            DataValidation validation =
                    helper.createValidation(
                            constraint,
                            addressList
                    );

            validation.setShowErrorBox(true);

            validation.setSuppressDropDownArrow(false);

            sheet.addValidationData(validation);

            // =====================================
            // 🔥 AUTO SIZE
            // =====================================

            for (int i = 0; i < columnas.length; i++) {

                sheet.autoSizeColumn(i);
            }

            // =====================================
            // 🔥 RESPUESTA
            // =====================================

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=plantilla_instructores.xlsx"
            );

            workbook.write(response.getOutputStream());

            response.getOutputStream().flush();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    
    @PostMapping("/eliminar-masivo")
    public String eliminarMasivo(@RequestParam List<Long> ids){

        for(Long id : ids){
            instructorService.eliminar(id); // soft delete
        }

        return "redirect:/instructores";
    }
    
    @PostMapping("/eliminar-definitivo")
    public String eliminarDefinitivo(@RequestParam List<Long> ids){

        for(Long id : ids){
            instructorService.eliminarDefinitivo(id);
        }

        return "redirect:/instructores/eliminados";
    }
   
}