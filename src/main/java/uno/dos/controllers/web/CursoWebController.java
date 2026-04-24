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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletResponse;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Instructores;
import uno.dos.models.entity.TipoCurso;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.services.TrabajadorService;
import uno.dos.services.CapacitacionService;
import uno.dos.services.CursoService;
import uno.dos.services.InscripcionService;
import uno.dos.services.InstructorService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoWebController {

    private final CursoService cursoService;
    private final InstructorService instructorService;
    private final InscripcionService inscripcionService;
    private final TrabajadorService trabajadorService;
    private final TemplateEngine templateEngine;
    private final CapacitacionService capacitacionService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cursos", cursoService.listarActivos());
        return "cursos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("curso", new Curso());
        model.addAttribute("instructores", instructorService.listarInstructores());
        return "cursos/form";
    }

    @PostMapping("/guardar")
    public String guardarCurso(@ModelAttribute Curso curso,
                               @RequestParam(required = false) List<Long> capacitacionesIds){

        // 🔥 DEFAULTS (ANTES DE GUARDAR)
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

        // 🔥 INSTRUCTOR
        if(curso.getInstructor() != null && curso.getInstructor().getId() != null){
            Instructores instructor = instructorService
                    .buscarPorId(curso.getInstructor().getId())
                    .orElse(null);
            curso.setInstructor(instructor);
        } else {
            curso.setInstructor(null);
        }

        // 🔥 VALIDACIÓN
        if(curso.getDuracion() != null && curso.getDuracion() < 0){
            curso.setDuracion(0);
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
    public String importarExcel(@RequestParam("archivo") MultipartFile archivo, Model model) {

        int importados = 0;
        int duplicados = 0;
        int vacios = 0;

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                try {

                    Row row = sheet.getRow(i);

                    if (row == null) {
                        vacios++;
                        continue;
                    }

                    String claveCurso = formatter.formatCellValue(row.getCell(1)).trim();
                    String nombreCurso = formatter.formatCellValue(row.getCell(2)).trim();

                    // 🔥 VALIDACIÓN CRÍTICA
                    if (claveCurso.isEmpty() || nombreCurso.isEmpty()) {
                        vacios++;
                        System.out.println("Fila " + i + " incompleta");
                        continue;
                    }

                    // 🔥 DUPLICADO
                    if (cursoService.existeClaveCurso(claveCurso)) {
                        duplicados++;
                        continue;
                    }

                    // 🔥 DURACIÓN SEGURA
                    Integer duracion = 0;
                    try {
                        String duracionTexto = formatter.formatCellValue(row.getCell(4)).trim();
                        if (!duracionTexto.isEmpty()) {
                            duracion = Integer.parseInt(duracionTexto);
                        }
                    } catch (Exception e) {
                        duracion = 0;
                    }

                    // 🔥 ENUMS SEGUROS
                    TipoCurso tipoCurso;
                    try {
                        String tipoCursoTexto = formatter.formatCellValue(row.getCell(7)).trim();
                        tipoCurso = TipoCurso.valueOf(tipoCursoTexto.toUpperCase());
                    } catch (Exception e) {
                        tipoCurso = TipoCurso.INTERNO;
                    }

                    TipoTrabajador tipoTrabajador;
                    try {
                        String tipoTrabajadorTexto = formatter.formatCellValue(row.getCell(8)).trim();
                        tipoTrabajador = TipoTrabajador.valueOf(tipoTrabajadorTexto.toUpperCase());
                    } catch (Exception e) {
                        tipoTrabajador = TipoTrabajador.AMBOS;
                    }

                    // 🔥 CREAR OBJETO
                    Curso curso = new Curso();
                    curso.setClaveCurso(claveCurso);
                    curso.setNombreCurso(nombreCurso);
                    curso.setDuracion(duracion);
                    curso.setTipoCurso(tipoCurso);
                    curso.setTipoTrabajador(tipoTrabajador);
                    curso.setActivo(true);

                    cursoService.guardar(curso);

                    importados++;

                } catch (Exception e) {

                    // 🔥 AQUÍ NO SE ROMPE NADA
                    System.out.println("Error en fila " + i + ": " + e.getMessage());

                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("mensaje",
                "Cursos importados: " + importados +
                " | Duplicados: " + duplicados +
                " | Vacíos: " + vacios);

        model.addAttribute("cursos", cursoService.listarActivos());

        return "cursos/lista";
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
    public String editar(@PathVariable Long id, Model model){

        Curso curso = cursoService
                .buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        model.addAttribute("curso", curso);

        model.addAttribute("instructores",
                instructorService.listarInstructores());

        return "cursos/form";
    }
    
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Curso curso){

        Curso original = cursoService.buscarPorId(curso.getId()).orElseThrow();

        // 🔥 conservar fechas si vienen null
        if(curso.getFechaInicio() == null){
            curso.setFechaInicio(original.getFechaInicio());
        }

        if(curso.getFechaFin() == null){
            curso.setFechaFin(original.getFechaFin());
        }

        // 🔥 RESOLVER INSTRUCTOR (IGUAL QUE EN GUARDAR)
        if(curso.getInstructor() != null && curso.getInstructor().getId() != null){

            Instructores inst = instructorService
                    .buscarPorId(curso.getInstructor().getId())
                    .orElse(null);

            curso.setInstructor(inst);
        } else {
            curso.setInstructor(null);
        }

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
    public void descargarPlantilla(HttpServletResponse response) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Cursos");

            // 🔥 ENCABEZADOS (SIN ACTIVO)
            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("claveInstitucion");
            header.createCell(1).setCellValue("claveCurso");
            header.createCell(2).setCellValue("nombreCurso");
            header.createCell(3).setCellValue("claveAreaTematica");
            header.createCell(4).setCellValue("duracion");
            header.createCell(5).setCellValue("tipoCurso");
            header.createCell(6).setCellValue("tipoTrabajador");
            header.createCell(7).setCellValue("areaResponsable");
            header.createCell(8).setCellValue("fechaInicio");
            header.createCell(9).setCellValue("fechaFin");

            // 🔥 EJEMPLO
            Row ejemplo = sheet.createRow(1);

            ejemplo.createCell(0).setCellValue("INST01");
            ejemplo.createCell(1).setCellValue("CUR001");
            ejemplo.createCell(2).setCellValue("Java Básico");
            ejemplo.createCell(3).setCellValue("SISTEMAS");
            ejemplo.createCell(4).setCellValue(40);
            ejemplo.createCell(5).setCellValue("INTERNO");
            ejemplo.createCell(6).setCellValue("AMBOS");
            ejemplo.createCell(7).setCellValue("TI");
            ejemplo.createCell(8).setCellValue("2026-03-01");
            ejemplo.createCell(9).setCellValue("2026-03-10");

            // =========================================
            // 🔥 VALIDACIONES (DROP DOWN)
            // =========================================

            DataValidationHelper helper = sheet.getDataValidationHelper();

            // Tipo Curso
            DataValidationConstraint tipoCursoConstraint =
                    helper.createExplicitListConstraint(new String[]{"INTERNO", "EXTERNO"});

            CellRangeAddressList tipoCursoRange =
                    new CellRangeAddressList(1, 100, 5, 5);

            sheet.addValidationData(helper.createValidation(tipoCursoConstraint, tipoCursoRange));

            // Tipo Trabajador
            DataValidationConstraint tipoTrabajadorConstraint =
                    helper.createExplicitListConstraint(new String[]{"SALARY", "HOURLY", "AMBOS"});

            CellRangeAddressList tipoTrabajadorRange =
                    new CellRangeAddressList(1, 100, 6, 6);

            sheet.addValidationData(helper.createValidation(tipoTrabajadorConstraint, tipoTrabajadorRange));

            // =========================================

            // 🔥 AUTO SIZE
            for (int i = 0; i <= 9; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🔥 RESPUESTA
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=plantilla_cursos.xlsx");

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

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
            horasRestantes.put(c.getId(), c.getDuracion() - usadas);
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
            horasRestantes.put(c.getId(), c.getDuracion() - usadas);
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