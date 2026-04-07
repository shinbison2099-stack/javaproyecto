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

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import uno.dos.models.entity.Rol;
import uno.dos.models.entity.Instructores;
import uno.dos.services.InstructorService;
import uno.dos.services.util.PdfService;

@Controller
@RequestMapping("/instructores")
@RequiredArgsConstructor
public class InstructorWebController {

    private final InstructorService instructorService;
    private final PdfService pdfService;

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

        model.addAttribute("instructor", new Instructores());

        return "instructores/form";
    }

    /* ===============================
       GUARDAR
    =============================== */

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Instructores instructor) {

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

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nombre = formatter.formatCellValue(row.getCell(0)).trim();
                String email = formatter.formatCellValue(row.getCell(1)).trim().toLowerCase();
                String password = formatter.formatCellValue(row.getCell(2)).trim();
                String rolTexto = formatter.formatCellValue(row.getCell(3)).trim();
                
                boolean activo = true;
                if (instructorService.existeEmail(email)) {
                    continue;
                }

                Rol rol;
                try {
                    rol = Rol.valueOf(rolTexto.toUpperCase());
                } catch (Exception e) {
                    System.out.println("Rol inválido: " + rolTexto);
                    continue;
                }

                Instructores instructor = new Instructores();
                instructor.setNombre(nombre);
                instructor.setEmail(email);
                instructor.setPassword(password);
                instructor.setRol(rol);
                instructor.setActivo(activo);

                instructorService.guardar(instructor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/instructores";
    }

    /* ===============================
       EXPORTAR PDF
    =============================== */

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportarPdf() {

        List<Instructores> instructores =
                instructorService.listarInstructores();

        Context context = new Context();
        context.setVariable("instructores", instructores);

        byte[] pdf = pdfService.generarPdf("instructores/pdf", context);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=instructores.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
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

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {

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

            // 🔥 ENCABEZADOS
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("nombre");
            header.createCell(1).setCellValue("email");
            header.createCell(2).setCellValue("password");
            header.createCell(3).setCellValue("rol");

            // 🔥 FILA EJEMPLO
            Row ejemplo = sheet.createRow(1);
            ejemplo.createCell(0).setCellValue("Juan Perez");
            ejemplo.createCell(1).setCellValue("juan@mail.com");
            ejemplo.createCell(2).setCellValue("1234");
            ejemplo.createCell(3).setCellValue("INSTRUCTOR");

            // =====================================
            // 🔥 AQUÍ VA LA VALIDACIÓN 👇
            // =====================================

            DataValidationHelper helper = sheet.getDataValidationHelper();

            DataValidationConstraint constraint =
                    helper.createExplicitListConstraint(new String[]{"INSTRUCTOR", "ADMIN"});

            // columna 3 = rol
            CellRangeAddressList addressList = new CellRangeAddressList(1, 100, 3, 3);

            DataValidation validation = helper.createValidation(constraint, addressList);
            validation.setShowErrorBox(true);

            sheet.addValidationData(validation);

            // =====================================

            // 🔥 AJUSTAR COLUMNAS
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🔥 RESPUESTA
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=plantilla_instructores.xlsx");

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   
}