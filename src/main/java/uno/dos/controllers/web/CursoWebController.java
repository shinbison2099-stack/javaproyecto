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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletResponse;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.TipoCurso;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.services.TrabajadorService;
import uno.dos.services.CursoService;
import uno.dos.services.InscripcionService;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoWebController {

    private final CursoService cursoService;
    private final InscripcionService inscripcionService;
    private final TrabajadorService trabajadorService;
    private final TemplateEngine templateEngine;
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cursos", cursoService.listarActivos());
        return "cursos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("curso", new Curso());
        
        return "cursos/form";
    }

    @PostMapping("/guardar")
    public String guardarCurso(@ModelAttribute Curso curso,
                               @RequestParam(required = false) List<Long> capacitacionesIds){

        // 🔥 DEFAULTS
        if(curso.getTipoCurso() == null){
            curso.setTipoCurso(TipoCurso.INTERNO);
        }

        if(curso.getTipoTrabajador() == null){
            curso.setTipoTrabajador(TipoTrabajador.AMBOS);
        }

        // 🔥 ACTIVO
        if(curso.getId() == null){
            curso.setActivo(true);
        }

        // 🔥 VALIDACIÓN HORAS
        if(curso.getHoras() != null && curso.getHoras() < 0){
            curso.setHoras(0);
        }

        // 🔥 VALIDACIÓN VIGENCIA
        if(curso.getVigenciaMeses() != null && curso.getVigenciaMeses() < 0){
            curso.setVigenciaMeses(0);
        }

        // 🔥 GUARDAR
        Curso guardado = cursoService.guardar(curso);

        // 🔥 CAPACITACIONES
        if(capacitacionesIds != null && !capacitacionesIds.isEmpty()){
            cursoService.asignarCapacitaciones(guardado.getId(), capacitacionesIds);
        }

        return "redirect:/cursos";
    }

    @GetMapping("/{id:\\d+}")
    public String verCurso(@PathVariable Long id, Model model) {

        Curso curso = cursoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        model.addAttribute("curso", curso);
        model.addAttribute("inscripciones", inscripcionService.listarPorCurso(id));
        model.addAttribute("trabajadores", trabajadorService.listarActivos());

        return "cursos/detalle";
    }

    @PostMapping("/{cursoId}/inscribir")
    public String inscribir(@PathVariable Long cursoId,
                            @RequestParam Long trabajadorId) {

        inscripcionService.inscribir(trabajadorId, cursoId);

        return "redirect:/cursos/" + cursoId;
    }

    @PostMapping("/{cursoId}/eliminar-inscripcion/{inscripcionId}")
    public String eliminarInscripcion(@PathVariable Long cursoId,
                                      @PathVariable Long inscripcionId) {

        inscripcionService.eliminar(inscripcionId);

        return "redirect:/cursos/" + cursoId;
    }

    @PostMapping("/importar")
    public String importarExcel(
            @RequestParam("archivo") MultipartFile archivo,
            RedirectAttributes redirectAttributes) {

        int importados = 0;
        int duplicados = 0;
        int vacios = 0;
        int errores = 0;

        try (Workbook workbook =
                     WorkbookFactory.create(
                             archivo.getInputStream())) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            for (int i = 1;
                 i <= sheet.getLastRowNum();
                 i++) {

                try {

                    Row row = sheet.getRow(i);

                    if (row == null) {
                        vacios++;
                        continue;
                    }

                    String claveCurso =
                            formatter.formatCellValue(
                                    row.getCell(1))
                                    .trim();

                    String nombreCurso =
                            formatter.formatCellValue(
                                    row.getCell(2))
                                    .trim();

                    if (claveCurso.isBlank()
                            || nombreCurso.isBlank()) {

                        vacios++;
                        continue;
                    }

                    if (cursoService.existeClaveCurso(
                            claveCurso)) {

                        duplicados++;
                        continue;
                    }

                    Curso curso = new Curso();

                    curso.setClaveInstitucion(
                            formatter.formatCellValue(
                                    row.getCell(0))
                                    .trim());

                    curso.setClaveCurso(
                            claveCurso);

                    curso.setNombreCurso(
                            nombreCurso);

                    curso.setClaveAreaTematica(
                            formatter.formatCellValue(
                                    row.getCell(3))
                                    .trim());

                    curso.setHoras(
                            obtenerEntero(
                                    row.getCell(4),
                                    formatter));

                    curso.setVigenciaMeses(
                            obtenerEntero(
                                    row.getCell(5),
                                    formatter));

                    try {

                        curso.setTipoCurso(
                                TipoCurso.valueOf(
                                        formatter
                                                .formatCellValue(
                                                        row.getCell(6))
                                                .trim()
                                                .toUpperCase()));

                    } catch (Exception e) {

                        curso.setTipoCurso(
                                TipoCurso.INTERNO);
                    }

                    try {

                        curso.setTipoTrabajador(
                                TipoTrabajador.valueOf(
                                        formatter
                                                .formatCellValue(
                                                        row.getCell(7))
                                                .trim()
                                                .toUpperCase()));

                    } catch (Exception e) {

                        curso.setTipoTrabajador(
                                TipoTrabajador.AMBOS);
                    }

                    curso.setAreaResponsable(
                            formatter.formatCellValue(
                                    row.getCell(8))
                                    .trim());

                    curso.setActivo(true);

                    cursoService.guardar(curso);

                    importados++;

                } catch (Exception e) {

                    errores++;

                    System.out.println(
                            "❌ Error fila "
                                    + i
                                    + ": "
                                    + e.getMessage());
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Error al procesar el archivo Excel");

            return "redirect:/cursos";
        }

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Importados: " + importados
                        + " | Duplicados: " + duplicados
                        + " | Vacíos: " + vacios
                        + " | Errores: " + errores);

        return "redirect:/cursos";
    }
    
    private Integer obtenerEntero(
            Cell cell,
            DataFormatter formatter) {

        try {

            String texto =
                    formatter
                            .formatCellValue(cell)
                            .trim()
                            .replaceAll("[^0-9]", "");

            return texto.isEmpty()
                    ? 0
                    : Integer.parseInt(texto);

        } catch (Exception e) {

            return 0;
        }
    }
    
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportarPdf() {

        try {

            List<Curso> cursos = cursoService.listarActivos();

            Context context = new Context();
            context.setVariable("cursos", cursos);

            String html = templateEngine.process("cursos/pdf", context);

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.useFastMode();

            builder.run();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=cursos.pdf")
                    .header("Content-Type", "application/pdf")
                    .body(os.toByteArray());

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }
    
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model){

        Curso curso =
                cursoService
                .buscarPorId(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Curso no encontrado"
                        )
                );

        model.addAttribute(
                "curso",
                curso
        );

        return "cursos/form";
    }
    
    @PostMapping("/actualizar")
    public String actualizar(
            @ModelAttribute Curso curso){

        curso.setActivo(true);

        cursoService.guardar(curso);

        return "redirect:/cursos";
    }
    
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        cursoService.eliminar(id);

        return "redirect:/cursos";
    }
    
    @GetMapping("/eliminados")
    public String verEliminados(Model model){

        model.addAttribute("cursos",
                cursoService.listarEliminados());

        return "cursos/eliminados";
    }
    
    @GetMapping("/restaurar/{id}")
    public String restaurar(@PathVariable Long id){

        Curso curso = cursoService
                .buscarPorId(id)
                .orElseThrow();

        curso.setActivo(true);

        cursoService.guardar(curso);

        return "redirect:/cursos/eliminados";
    }
    
    @GetMapping("/plantilla")
    public void descargarPlantilla(
            HttpServletResponse response) {

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Cursos"
                    );

            // =========================================
            // ENCABEZADOS
            // =========================================

            Row header =
                    sheet.createRow(0);

            header.createCell(0)
                    .setCellValue(
                            "claveInstitucion"
                    );

            header.createCell(1)
                    .setCellValue(
                            "claveCurso"
                    );

            header.createCell(2)
                    .setCellValue(
                            "nombreCurso"
                    );

            header.createCell(3)
                    .setCellValue(
                            "claveAreaTematica"
                    );

            header.createCell(4)
                    .setCellValue(
                            "horas"
                    );

            header.createCell(5)
                    .setCellValue(
                            "vigenciaMeses"
                    );

            header.createCell(6)
                    .setCellValue(
                            "tipoCurso"
                    );

            header.createCell(7)
                    .setCellValue(
                            "tipoTrabajador"
                    );

            header.createCell(8)
                    .setCellValue(
                            "areaResponsable"
                    );

            // =========================================
            // EJEMPLO 1
            // =========================================

            Row ejemplo1 =
                    sheet.createRow(1);

            ejemplo1.createCell(0)
                    .setCellValue(
                            "INST01"
                    );

            ejemplo1.createCell(1)
                    .setCellValue(
                            "CUR001"
                    );

            ejemplo1.createCell(2)
                    .setCellValue(
                            "Java Básico"
                    );

            ejemplo1.createCell(3)
                    .setCellValue(
                            "SISTEMAS"
                    );

            ejemplo1.createCell(4)
                    .setCellValue(
                            8
                    );

            ejemplo1.createCell(5)
                    .setCellValue(
                            36
                    );

            ejemplo1.createCell(6)
                    .setCellValue(
                            "INTERNO"
                    );

            ejemplo1.createCell(7)
                    .setCellValue(
                            "AMBOS"
                    );

            ejemplo1.createCell(8)
                    .setCellValue(
                            "TI"
                    );

            // =========================================
            // EJEMPLO 2
            // =========================================

            Row ejemplo2 =
                    sheet.createRow(2);

            ejemplo2.createCell(0)
                    .setCellValue(
                            "INST01"
                    );

            ejemplo2.createCell(1)
                    .setCellValue(
                            "CUR002"
                    );

            ejemplo2.createCell(2)
                    .setCellValue(
                            "Spring Boot"
                    );

            ejemplo2.createCell(3)
                    .setCellValue(
                            "DESARROLLO"
                    );

            ejemplo2.createCell(4)
                    .setCellValue(
                            24
                    );

            ejemplo2.createCell(5)
                    .setCellValue(
                            24
                    );

            ejemplo2.createCell(6)
                    .setCellValue(
                            "INTERNO"
                    );

            ejemplo2.createCell(7)
                    .setCellValue(
                            "SALARY"
                    );

            ejemplo2.createCell(8)
                    .setCellValue(
                            "TI"
                    );

            // =========================================
            // VALIDACION TIPO CURSO
            // =========================================

            DataValidationHelper helper =
                    sheet.getDataValidationHelper();

            DataValidationConstraint tipoCursoConstraint =
                    helper.createExplicitListConstraint(
                            new String[]{
                                    "INTERNO",
                                    "EXTERNO"
                            }
                    );

            CellRangeAddressList tipoCursoRange =
                    new CellRangeAddressList(
                            1,
                            1000,
                            6,
                            6
                    );

            sheet.addValidationData(
                    helper.createValidation(
                            tipoCursoConstraint,
                            tipoCursoRange
                    )
            );

            // =========================================
            // VALIDACION TIPO TRABAJADOR
            // =========================================

            DataValidationConstraint tipoTrabajadorConstraint =
                    helper.createExplicitListConstraint(
                            new String[]{
                                    "SALARY",
                                    "HOURLY",
                                    "AMBOS"
                            }
                    );

            CellRangeAddressList tipoTrabajadorRange =
                    new CellRangeAddressList(
                            1,
                            1000,
                            7,
                            7
                    );

            sheet.addValidationData(
                    helper.createValidation(
                            tipoTrabajadorConstraint,
                            tipoTrabajadorRange
                    )
            );

            // =========================================
            // AUTO SIZE
            // =========================================

            for (int i = 0;
                 i <= 8;
                 i++) {

                sheet.autoSizeColumn(i);
            }

            // =========================================
            // DESCARGA
            // =========================================

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=plantilla_cursos.xlsx"
            );

            workbook.write(
                    response.getOutputStream()
            );

            response.getOutputStream()
                    .flush();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    
    @GetMapping("/asignados")
    public String cursosAsignados(Model model){

        List<Curso> cursos = cursoService.listarConCapacitaciones();

        Map<Long, Integer> horasUsadas = new HashMap<>();
        Map<Long, Integer> horasRestantes = new HashMap<>();

        for(Curso c : cursos){

            int usadas = c.getCapacitaciones()
                    .stream()
                    .mapToInt(cap -> cap.getDuracionHoras() != null ? cap.getDuracionHoras() : 0)
                    .sum();

            horasUsadas.put(c.getId(), usadas);

            // 🔥 CORRECCIÓN AQUÍ
            int horasCurso = c.getHoras() != null ? c.getHoras() : 0;
            horasRestantes.put(c.getId(), horasCurso - usadas);
        }

        model.addAttribute("cursos", cursos);
        model.addAttribute("horasUsadas", horasUsadas);
        model.addAttribute("horasRestantes", horasRestantes);

        return "cursos/asignados";
    }
    
    @GetMapping("/cursos-capacitaciones")
    public String ver(Model model){

        List<Curso> cursos = cursoService.listarActivos();

        Map<Long, Integer> horasUsadas = new HashMap<>();
        Map<Long, Integer> horasRestantes = new HashMap<>();

        for(Curso c : cursos){

            int usadas = cursoService.horasUsadas(c.getId());

            horasUsadas.put(c.getId(), usadas);

            // 🔥 CORRECCIÓN
            int horasCurso = c.getHoras() != null ? c.getHoras() : 0;
            horasRestantes.put(c.getId(), horasCurso - usadas);
        }

        model.addAttribute("cursos", cursos);
        model.addAttribute("horasUsadas", horasUsadas);
        model.addAttribute("horasRestantes", horasRestantes);

        return "cursos/capacitaciones";
    }
    
    @PostMapping("/eliminar-masivo")
    public String eliminarMasivo(@RequestParam List<Long> ids){

        for(Long id : ids){
            cursoService.eliminarDefinitivo(id);
        }

        return "redirect:/cursos/eliminados";
    }
    
    
    
}