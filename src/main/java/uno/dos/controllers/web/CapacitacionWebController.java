package uno.dos.controllers.web;

import java.time.LocalDate;
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
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.models.entity.Instructores;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.CapacitacionTrabajadorRepository;
import uno.dos.repositories.EvaluacionCapacitacionRepository;
import uno.dos.services.CapacitacionService;
import uno.dos.services.InscripcionService;
import uno.dos.services.InstructorService;
import uno.dos.services.TrabajadorService;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
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
    public String guardar(@ModelAttribute Capacitacion capacitacion,
                          @RequestParam Long instructor){

        Instructores inst =
                instructorService.buscarPorId(instructor).orElseThrow();

        capacitacion.setInstructor(inst);

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
    public String actualizar(@ModelAttribute Capacitacion capacitacion,
                             @RequestParam Long instructor){

        Instructores inst =
                instructorService.buscarPorId(instructor).orElseThrow();

        capacitacion.setInstructor(inst);

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
    public String verCapacitacion(@PathVariable Long id, Model model){

        Capacitacion capacitacion = capacitacionService
                .buscarPorId(id)
                .orElseThrow();

        List<CapacitacionTrabajador> inscripciones = capacitacionTrabajadorRepository.findByCapacitacionIdAndActivoTrue(id);

        Map<Long, EvaluacionCapacitacion> evaluaciones = new HashMap<>();

        for(CapacitacionTrabajador ins : inscripciones){

            List<EvaluacionCapacitacion> lista = evaluacionRepository
                    .findByTrabajador_IdAndCapacitacion_Id(
                            ins.getTrabajador().getId(),
                            capacitacion.getId()
                    );

            EvaluacionCapacitacion ev = lista.isEmpty() ? null : lista.get(0);

            evaluaciones.put(ins.getTrabajador().getId(), ev);
        }

        // 🔥 ESTA LÍNEA TE FALTABA
        model.addAttribute("trabajadores",
                trabajadorService.listarActivos());

        model.addAttribute("capacitacion", capacitacion);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("evaluaciones", evaluaciones);

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

                    // 🔴 FILA VACÍA
                    if (clave.isBlank()) continue;

                    // 🔴 DUPLICADO
                    if (capacitacionService.existeClave(clave)) {
                        duplicados++;
                        continue;
                    }

                    Capacitacion cap = new Capacitacion();

                    cap.setClaveCapacitacion(clave);

                    cap.setNombreCapacitacion(
                            formatter.formatCellValue(row.getCell(1)).trim());

                    cap.setDescripcion(
                            formatter.formatCellValue(row.getCell(2)).trim());

                    cap.setTipoCapacitacion(
                            formatter.formatCellValue(row.getCell(3)).trim().toUpperCase());

                    cap.setModalidad(
                            formatter.formatCellValue(row.getCell(4)).trim().toUpperCase());

                    // 🔢 DURACIÓN
                    String duracion = formatter.formatCellValue(row.getCell(5)).trim();
                    if (!duracion.isBlank()) {
                        cap.setDuracionHoras(Integer.parseInt(duracion));
                    }

                    // 📅 FECHA INICIO (EXCEL NUMÉRICO)
                    if (row.getCell(6) != null &&
                            row.getCell(6).getCellType() == CellType.NUMERIC &&
                            DateUtil.isCellDateFormatted(row.getCell(6))) {

                        cap.setFechaInicio(
                                row.getCell(6).getLocalDateTimeCellValue().toLocalDate());
                    }

                    // 📅 FECHA FIN
                    if (row.getCell(7) != null &&
                            row.getCell(7).getCellType() == CellType.NUMERIC &&
                            DateUtil.isCellDateFormatted(row.getCell(7))) {

                        cap.setFechaFin(
                                row.getCell(7).getLocalDateTimeCellValue().toLocalDate());
                    }

                    // 📆 VIGENCIA
                    String vigencia = formatter.formatCellValue(row.getCell(8)).trim();
                    if (!vigencia.isBlank()) {
                        cap.setVigenciaMeses(Integer.parseInt(vigencia));
                    }

                    // 👨‍🏫 INSTRUCTOR
                    String nombreInstructor =
                            formatter.formatCellValue(row.getCell(9)).trim();

                    if (!nombreInstructor.isBlank()) {

                        Instructores inst =
                                instructorService.buscarPorNombre(nombreInstructor);

                        if (inst != null) {
                            cap.setInstructor(inst);
                        } else {
                            System.out.println("Instructor no encontrado: " + nombreInstructor);
                        }
                    }

                    cap.setActivo(true);

                    capacitacionService.guardar(cap);

                    importados++;

                } catch (Exception e) {

                    errores++;

                    System.out.println("❌ Error en fila: " + i);
                    e.printStackTrace();
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
    public String verTrabajadores(@PathVariable Long id, Model model){

        Capacitacion cap = capacitacionService.buscarPorId(id).orElseThrow();

        List<Trabajador> disponibles =
                trabajadorService.disponiblesParaCapacitacion(id);

        model.addAttribute("capacitacion", cap);

        // 🔥 SOLO ESTA LÍNEA
        model.addAttribute("trabajadores", disponibles);

        return "capacitaciones/asignar-trabajadores";
    }
    
        @PostMapping("/asignar")
        public String asignarTrabajadores(@RequestParam Long capId,
                                         @RequestParam List<Long> trabajadoresIds){

            for(Long trabId : trabajadoresIds){
                inscripcionService.asignarCapacitacion(trabId, capId);
            }

            // 🔥 REDIRECT CORRECTO
            return "redirect:/cursos/asignados";
        }
        
       

}