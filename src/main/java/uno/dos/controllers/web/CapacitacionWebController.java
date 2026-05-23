package uno.dos.controllers.web;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.models.entity.Instructores;
import uno.dos.models.entity.NivelSkill;
import uno.dos.models.entity.SkillMatrix;
import uno.dos.models.entity.TipoInstructor;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.CapacitacionTrabajadorRepository;
import uno.dos.repositories.EvaluacionCapacitacionRepository;
import uno.dos.services.CapacitacionService;
import uno.dos.services.InscripcionService;
import uno.dos.services.InstructorService;
import uno.dos.services.SkillMatrixService;
import uno.dos.services.TrabajadorService;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.TemplateEngine;

@Controller
@RequestMapping("/capacitaciones")
@RequiredArgsConstructor
public class CapacitacionWebController {

    private final CapacitacionService capacitacionService;
    private final InstructorService instructorService;
    private final TrabajadorService trabajadorService;
    private final CapacitacionTrabajadorRepository capacitacionTrabajadorRepository;
    private final TemplateEngine templateEngine;
    private final EvaluacionCapacitacionRepository evaluacionRepository;
    private final InscripcionService inscripcionService;
    private final SkillMatrixService skillMatrixService;
    /* ===============================
       LISTAR
       =============================== */

    @GetMapping
    public String listar(Model model){

        model.addAttribute("capacitaciones",
                capacitacionService.listarActivas());

        return "capacitaciones/lista";
    }

    /* ===============================
       NUEVO
       =============================== */

    @GetMapping("/nuevo")
    public String nuevo(Model model){

        model.addAttribute("capacitacion", new Capacitacion());

        model.addAttribute("instructores", instructorService.listarInstructores());

        return "capacitaciones/form";
    }

    /* ===============================
       GUARDAR
       =============================== */

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Capacitacion capacitacion){

        capacitacion.setActivo(true);

        capacitacionService.guardar(capacitacion);

        return "redirect:/capacitaciones";
    }

    /* ===============================
       EDITAR
       =============================== */

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model){

        Capacitacion cap =
                capacitacionService.buscarPorId(id).orElseThrow();

        model.addAttribute("capacitacion", cap);

        model.addAttribute("instructores",
                instructorService.listarInstructores());

        return "capacitaciones/form";
    }

    /* ===============================
       ACTUALIZAR
       =============================== */

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Capacitacion capacitacion){

        capacitacionService.guardar(capacitacion);

        return "redirect:/capacitaciones";
    }
    /* ===============================
       ELIMINAR
       =============================== */

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        capacitacionService.eliminar(id);

        return "redirect:/capacitaciones";
    }

    /* ===============================
       VER CAPACITACION
       =============================== */

    @GetMapping("/{id:[0-9]+}")
    public String verCapacitacion(@PathVariable Long id,
                                  Model model){

        Capacitacion capacitacion = capacitacionService
                .buscarPorId(id)
                .orElseThrow();

        // =====================================
        // 🔥 SOLO SALARY Y AMBOS
        // =====================================

        if(capacitacion.getTipoTrabajador()
                == TipoTrabajador.HOURLY){

            return "redirect:/skillmatrix";
        }

        // =====================================
        // 🔥 INSCRIPCIONES
        // =====================================

        List<CapacitacionTrabajador> inscripciones =
                capacitacionTrabajadorRepository
                .findByCapacitacionIdAndActivoTrue(id);

        Map<Long, EvaluacionCapacitacion> evaluaciones =
                new HashMap<>();

        for(CapacitacionTrabajador ins : inscripciones){

            List<EvaluacionCapacitacion> lista =
                    evaluacionRepository
                    .findByTrabajador_IdAndCapacitacion_Id(

                            ins.getTrabajador().getId(),
                            capacitacion.getId()
                    );

            EvaluacionCapacitacion ev =
                    lista.isEmpty() ? null : lista.get(0);

            evaluaciones.put(
                    ins.getTrabajador().getId(),
                    ev
            );
        }

        // =====================================
        // 🔥 SOLO TRABAJADORES SALARY
        // =====================================

        model.addAttribute(
                "trabajadores",

                trabajadorService.buscarSalaryYAmbos()
        );

        model.addAttribute(
                "capacitacion",
                capacitacion
        );

        model.addAttribute(
                "inscripciones",
                inscripciones
        );

        model.addAttribute(
                "evaluaciones",
                evaluaciones
        );

        return "capacitaciones/detalle";
    }

    /* ===============================
       INSCRIBIR TRABAJADOR
       =============================== */

    @PostMapping("/{capId}/inscribir")
    public String inscribir(
            @PathVariable Long capId,
            @RequestParam Long trabajadorId){

        boolean existe = capacitacionTrabajadorRepository
                .existsByTrabajador_IdAndCapacitacion_Id(trabajadorId, capId);

        if(existe){
            return "redirect:/capacitaciones/" + capId + "?error";
        }

        CapacitacionTrabajador ins = new CapacitacionTrabajador();

        ins.setCapacitacion(
                capacitacionService.buscarPorId(capId).orElseThrow());

        ins.setTrabajador(
                trabajadorService.buscarPorId(trabajadorId).orElseThrow());

        ins.setFechaInscripcion(LocalDate.now());

        capacitacionTrabajadorRepository.save(ins);

        return "redirect:/capacitaciones/" + capId;
    }
    
    @GetMapping("/restaurar/{id}")
    public String restaurar(@PathVariable Long id){

        Capacitacion cap =
                capacitacionService.buscarPorId(id).orElseThrow();

        cap.setActivo(true);

        capacitacionService.guardar(cap);

        return "redirect:/capacitaciones/eliminadas";
    }
    
    @PostMapping("/importar")
    public String importarExcel(@RequestParam("archivo") MultipartFile archivo){

        int importados = 0;
        int duplicados = 0;
        int errores = 0;

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                try {

                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String clave = formatter.formatCellValue(row.getCell(0)).trim();

                    // 🔴 VACÍO
                    if (clave.isBlank()) continue;

                    // 🔴 DUPLICADO
                    if (capacitacionService.existeClave(clave)) {
                        duplicados++;
                        continue;
                    }

                    Capacitacion cap = new Capacitacion();

                    cap.setClaveCapacitacion(clave);
                    cap.setNombreCapacitacion(formatter.formatCellValue(row.getCell(1)).trim());
                    cap.setDescripcion(formatter.formatCellValue(row.getCell(2)).trim());
                    cap.setTipoCapacitacion(formatter.formatCellValue(row.getCell(3)).trim().toUpperCase());
                    cap.setModalidad(formatter.formatCellValue(row.getCell(4)).trim().toUpperCase());

                    // 🔢 DURACIÓN
                    cap.setDuracionHoras(parseEntero(row.getCell(5), formatter));

                    // 📅 FECHAS
                    cap.setFechaInicio(parseFecha(row.getCell(6), formatter));
                    cap.setFechaFin(parseFecha(row.getCell(7), formatter));

                    // 📆 VIGENCIA
                    cap.setVigenciaMeses(parseEntero(row.getCell(8), formatter));
                    
                 // 🔥 TIPO TRABAJADOR
                    try {

                        String tipoTexto =
                                formatter.formatCellValue(row.getCell(10))
                                        .trim()
                                        .toUpperCase();

                        cap.setTipoTrabajador(
                                TipoTrabajador.valueOf(tipoTexto)
                        );

                    } catch (Exception e){

                        cap.setTipoTrabajador(TipoTrabajador.AMBOS);
                    }

                    // 👨‍🏫 INSTRUCTOR (SOLUCIÓN REAL 🔥)
                    String nombreInstructor = formatter.formatCellValue(row.getCell(9)).trim();

                    if (!nombreInstructor.isBlank()) {

                        Instructores inst = instructorService.buscarPorNombre(nombreInstructor);

                        if (inst == null) {

                            // 🔥 CREAR CON DATOS COMPLETOS (EVITA ERROR NOT NULL)
                            inst = new Instructores();

                            inst.setNombre(nombreInstructor);

                            // 🔥 EMAIL AUTOMÁTICO
                            String email = nombreInstructor
                                    .toLowerCase()
                                    .replace(" ", ".") + "@empresa.com";

                            inst.setEmail(email);

                            // 🔥 PASSWORD DEFAULT
                            inst.setPassword("123456");

                            // 🔥 TIPO DEFAULT (ajusta si usas enum)
                            inst.setTipo(TipoInstructor.INTERNO);

                            inst.setActivo(true);

                            inst = instructorService.guardar(inst);

                            System.out.println("🆕 Instructor creado: " + nombreInstructor);
                        }

                        cap.setInstructor(inst);
                    }

                    cap.setActivo(true);

                    capacitacionService.guardar(cap);
                    importados++;

                } catch (Exception e) {
                    errores++;
                    System.out.println("❌ Error en fila " + i + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("✅ Importados: " + importados);
        System.out.println("⚠️ Duplicados: " + duplicados);
        System.out.println("❌ Errores: " + errores);

        return "redirect:/capacitaciones";
    }
    
    @GetMapping("/plantilla")
    public void descargarPlantilla(HttpServletResponse response) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Capacitaciones");

            // 🔥 ENCABEZADOS (ORDEN EXACTO DE TU IMPORT)
            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("claveCapacitacion");
            header.createCell(1).setCellValue("nombreCapacitacion");
            header.createCell(2).setCellValue("descripcion");
            header.createCell(3).setCellValue("tipoCapacitacion");
            header.createCell(4).setCellValue("modalidad");
            header.createCell(5).setCellValue("duracionHoras");
            header.createCell(6).setCellValue("fechaInicio");
            header.createCell(7).setCellValue("fechaFin");
            header.createCell(8).setCellValue("vigenciaMeses");
            header.createCell(9).setCellValue("instructor");
            header.createCell(10).setCellValue("tipoTrabajador");

            // 🔥 EJEMPLO
            Row ejemplo = sheet.createRow(1);

            ejemplo.createCell(0).setCellValue("CAP001");
            ejemplo.createCell(1).setCellValue("Seguridad Industrial");
            ejemplo.createCell(2).setCellValue("Uso de equipo de seguridad");
            ejemplo.createCell(3).setCellValue("TECNICA");
            ejemplo.createCell(4).setCellValue("PRESENCIAL");
            ejemplo.createCell(5).setCellValue(8);
            ejemplo.createCell(6).setCellValue("2026-01-10");
            ejemplo.createCell(7).setCellValue("2026-01-12");
            ejemplo.createCell(8).setCellValue(12);
            ejemplo.createCell(9).setCellValue("Juan Perez");
            ejemplo.createCell(10).setCellValue("AMBOS");

            // =========================================
            // 🔥 VALIDACIONES (DROP DOWN)
            // =========================================

            DataValidationHelper helper = sheet.getDataValidationHelper();

            // Tipo Capacitación
            DataValidationConstraint tipoConstraint =
                    helper.createExplicitListConstraint(
                            new String[]{"TECNICA", "ADMINISTRATIVA", "SEGURIDAD"});

            CellRangeAddressList tipoRange =
                    new CellRangeAddressList(1, 100, 3, 3);

            sheet.addValidationData(helper.createValidation(tipoConstraint, tipoRange));

            // Modalidad
            DataValidationConstraint modalidadConstraint =
                    helper.createExplicitListConstraint(
                            new String[]{"PRESENCIAL", "ONLINE", "MIXTA"});

            CellRangeAddressList modalidadRange =
                    new CellRangeAddressList(1, 100, 4, 4);

            sheet.addValidationData(helper.createValidation(modalidadConstraint, modalidadRange));
            
         // 🔥 Tipo Trabajador
            DataValidationConstraint tipoTrabajadorConstraint =
                    helper.createExplicitListConstraint(
                            new String[]{"SALARY", "HOURLY", "AMBOS"});

            CellRangeAddressList tipoTrabajadorRange =
                    new CellRangeAddressList(1, 100, 10, 10);

            sheet.addValidationData(
                    helper.createValidation(
                            tipoTrabajadorConstraint,
                            tipoTrabajadorRange
                    )
            );

            // =========================================

            // 🔥 AUTO SIZE
            for (int i = 0; i <= 10; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🔥 RESPUESTA
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=plantilla_capacitaciones.xlsx");

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportarPdf() throws Exception{

        List<Capacitacion> capacitaciones =
                capacitacionService.listarActivas();

        Context context = new Context();

        context.setVariable("capacitaciones", capacitaciones);

        String html =
                templateEngine.process("capacitaciones/pdf", context);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.withHtmlContent(html, null);

        builder.toStream(os);

        builder.run();

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=capacitaciones.pdf")
                .header("Content-Type","application/pdf")
                .body(os.toByteArray());
    }
    
    @GetMapping("/{capId}/desinscribir/{trabajadorId}")
    public String desinscribir(
            @PathVariable Long capId,
            @PathVariable Long trabajadorId){

        inscripcionService.desinscribir(trabajadorId, capId);

        return "redirect:/capacitaciones/" + capId;
    }
    
    @GetMapping("/{capId}/restaurar-inscripcion/{trabajadorId}")
    public String restaurarInscripcion(
            @PathVariable Long capId,
            @PathVariable Long trabajadorId){

        inscripcionService.restaurarInscripcion(trabajadorId, capId);

        return "redirect:/capacitaciones/" + capId;
    }
    
    @GetMapping("/restaurar-capacitacion/{id}")
    public String restaurarCapacitacion(@PathVariable Long id){

        capacitacionService.restaurarCapacitacion(id); // ✅ ESTE

        return "redirect:/capacitaciones";
    }
    
    @GetMapping("/{id}/eliminados")
    public String verEliminados(@PathVariable Long id, Model model){

        model.addAttribute("capacitacion",
                capacitacionService.buscarPorId(id).orElseThrow());

        model.addAttribute("inscripciones",
                capacitacionTrabajadorRepository
                        .findByCapacitacionIdAndActivoFalse(id));

        return "capacitaciones/eliminados";
    }
    
    @GetMapping("/eliminadas")
    public String verCapacitacionesEliminadas(Model model){

        model.addAttribute("capacitaciones",
                capacitacionService.listarEliminadas());

        return "capacitaciones/eliminadas";
    }
    
    
    @GetMapping("/{id}/trabajadores")
    public String verTrabajadores(
            @PathVariable Long id,
            Model model){

        // =====================================
        // 🔥 CAPACITACIÓN
        // =====================================

        Capacitacion cap =
                capacitacionService
                        .buscarPorId(id)
                        .orElseThrow();

        // =====================================
        // 🔥 DISPONIBLES
        // =====================================

        List<Trabajador> disponibles =
                trabajadorService
                        .disponiblesParaCapacitacion(id);

        // =====================================
        // 🔥 HOURLY
        // =====================================

        if(cap.getTipoTrabajador()
                == TipoTrabajador.HOURLY){

            disponibles =
                    disponibles.stream()

                            .filter(t ->

                                    t.getTipoTrabajador()
                                            == TipoTrabajador.HOURLY

                                    ||

                                    t.getTipoTrabajador()
                                            == TipoTrabajador.AMBOS
                            )

                            .toList();
        }

        // =====================================
        // 🔥 SALARY
        // =====================================

        else if(cap.getTipoTrabajador()
                == TipoTrabajador.SALARY){

            disponibles =
                    disponibles.stream()

                            .filter(t ->

                                    t.getTipoTrabajador()
                                            == TipoTrabajador.SALARY

                                    ||

                                    t.getTipoTrabajador()
                                            == TipoTrabajador.AMBOS
                            )

                            .toList();
        }

        // =====================================
        // 🔥 AMBOS
        // =====================================

        else if(cap.getTipoTrabajador()
                == TipoTrabajador.AMBOS){

            disponibles =
                    disponibles.stream()

                            .filter(t ->

                                    t.getTipoTrabajador()
                                            == TipoTrabajador.HOURLY

                                    ||

                                    t.getTipoTrabajador()
                                            == TipoTrabajador.SALARY

                                    ||

                                    t.getTipoTrabajador()
                                            == TipoTrabajador.AMBOS
                            )

                            .toList();
        }

        // =====================================

        model.addAttribute(
                "capacitacion",
                cap
        );

        model.addAttribute(
                "trabajadores",
                disponibles
        );

        return "capacitaciones/asignar-trabajadores";
    }
    
    @PostMapping("/asignar")
    public String asignarTrabajadores(

            @RequestParam Long capId,

            @RequestParam List<Long> trabajadoresIds){

        // =====================================
        // 🔥 BUSCAR CAPACITACIÓN
        // =====================================

        Capacitacion capacitacion =
                capacitacionService
                .buscarPorId(capId)
                .orElseThrow();

        for(Long trabId : trabajadoresIds){

            // =====================================
            // 🔥 INSCRIPCIÓN NORMAL
            // =====================================

            inscripcionService
                    .asignarCapacitacion(
                            trabId,
                            capId
                    );

            // =====================================
            // 🔥 BUSCAR TRABAJADOR
            // =====================================

            Trabajador trabajador =
                    trabajadorService
                    .buscarPorId(trabId)
                    .orElseThrow();

            // =====================================
            // 🔥 SI ES HOURLY
            // =====================================

            if(capacitacion.getTipoTrabajador()
                    == TipoTrabajador.HOURLY){

                Optional<SkillMatrix> existente =

                        skillMatrixService
                        .buscarPorTrabajadorYCapacitacion(

                                trabajador.getId(),
                                capacitacion.getId()
                        );

                // =====================================
                // 🔥 EVITAR DUPLICADOS
                // =====================================

                if(existente.isEmpty()){

                    SkillMatrix skill =
                            new SkillMatrix();

                    skill.setTrabajador(trabajador);

                    skill.setCapacitacion(capacitacion);

                    // 🔥 INICIA VACIO
                    skill.setNivel(
                            NivelSkill.VACIO
                    );

                    skill.setActivo(true);

                    skillMatrixService.guardar(skill);
                }
            }
        }

        // =====================================
        // 🔥 REDIRECT
        // =====================================

        return "redirect:/cursos/asignados";
    }
        
        @GetMapping("/eliminar-definitivo/{id}")
        public String eliminarDefinitivo(@PathVariable Long id,
                                        RedirectAttributes flash) {

            try {
                capacitacionService.eliminarDefinitivo(id);
                flash.addFlashAttribute("success", "Capacitación eliminada definitivamente");
            } catch (Exception e) {
                flash.addFlashAttribute("error", e.getMessage());
            }

            return "redirect:/capacitaciones/eliminadas";
        }
        
        
        
        private Integer parseEntero(Cell cell, DataFormatter formatter){
            try {
                String texto = formatter.formatCellValue(cell).trim();
                texto = texto.replaceAll("[^0-9]", ""); // limpia "12 meses"
                return texto.isBlank() ? 0 : Integer.parseInt(texto);
            } catch (Exception e){
                return 0;
            }
        }
        
        private LocalDate parseFecha(Cell cell, DataFormatter formatter){

            try {

                if (cell == null) return null;

                if (cell.getCellType() == CellType.NUMERIC &&
                    DateUtil.isCellDateFormatted(cell)) {

                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }

                String texto = formatter.formatCellValue(cell).trim();

                if (!texto.isBlank()) {
                    return LocalDate.parse(texto);
                }

            } catch (Exception e){
                return null;
            }

            return null;
        }
       

}