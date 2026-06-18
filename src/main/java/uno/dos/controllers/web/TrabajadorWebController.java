package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.models.entity.Inscripcion;
import uno.dos.models.entity.Puesto;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.CapacitacionTrabajadorRepository;
import uno.dos.repositories.InscripcionRepository;
import uno.dos.services.TrabajadorService;
import uno.dos.services.AreaService;
import uno.dos.services.EvaluacionService;
import uno.dos.services.MatrizService;
import uno.dos.services.PuestoService;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/trabajadores")
@RequiredArgsConstructor
public class TrabajadorWebController {

    private final TrabajadorService trabajadorService;
    private final TemplateEngine templateEngine;
    private final PuestoService puestoService;
    private final InscripcionRepository inscripcionRepository;
    private final MatrizService matrizService;
    private final EvaluacionService evaluacionService;
    private final CapacitacionTrabajadorRepository capacitacionTrabajadorRepository;
    private final AreaService areaService;

    @Value("${ruta.fotos}")
    private String rutaFotos;

    /* ===============================
       LISTAR TRABAJADORES
       =============================== */

    @GetMapping
    public String listar(Model model) {

        List<Trabajador> trabajadores = trabajadorService.listarActivos();

        model.addAttribute("trabajadores", trabajadores);

        // 🔥 NUEVO: MAPA DE INSCRIPCIONES
        Map<Long, List<Inscripcion>> inscripcionesPorTrabajador = new HashMap<>();

        for (Trabajador t : trabajadores) {

            List<Inscripcion> ins = inscripcionRepository.findByTrabajadorId(t.getId());

            inscripcionesPorTrabajador.put(t.getId(), ins);
        }

        model.addAttribute("inscripcionesPorTrabajador", inscripcionesPorTrabajador);

        return "trabajadores/lista";
    }

    /* ===============================
       FORMULARIO NUEVO
       =============================== */

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("trabajador", new Trabajador());
        model.addAttribute("areas",areaService.listar());
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

            Path ruta = Paths.get("uploads/trabajadores/" + nombreArchivo);

            Files.createDirectories(ruta.getParent());

            ImageIO.write(imagenRedimensionada,"jpg",ruta.toFile());

            trabajador.setFoto(nombreArchivo);
        }
        
     // 🔥 RECONSTRUIR PUESTO
        if(trabajador.getPuesto() != null && trabajador.getPuesto().getId() != null){
        	Puesto p = puestoService
        	        .buscarPorId(
        	                trabajador.getPuesto().getId()
        	        )
        	        .orElseThrow();

        	trabajador.setPuesto(p);
        }

        trabajador.setActivo(true);

        trabajadorService.guardar(trabajador);

        return "redirect:/trabajadores";
    }

    @PostMapping("/importar")
    public String importarExcel(
            @RequestParam("archivo")
            MultipartFile archivo) {

        int importados = 0;
        int duplicados = 0;
        int vacios = 0;

        try (Workbook workbook =
                     WorkbookFactory.create(
                             archivo.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

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

                    // =====================================
                    // 🔥 LEER COLUMNAS
                    // =====================================

                    String numeroPersonal =
                            formatter.formatCellValue(
                                    row.getCell(0)
                            ).trim();

                    String curp =
                            formatter.formatCellValue(
                                    row.getCell(1)
                            ).trim();

                    String nombreCompleto =
                            formatter.formatCellValue(
                                    row.getCell(2)
                            ).trim();

                    String areaEmpleado =
                            formatter.formatCellValue(
                                    row.getCell(3)
                            ).trim();

                    String puestoTexto =
                            formatter.formatCellValue(
                                    row.getCell(4)
                            ).trim();

                    // 🔥 NUEVO
                    String tipoTexto =
                            formatter.formatCellValue(
                                    row.getCell(5)
                            ).trim();

                    String email =
                            formatter.formatCellValue(
                                    row.getCell(6)
                            ).trim();

                    // =====================================
                    // 🔥 VALIDACIONES
                    // =====================================

                    if (curp.isBlank()
                            || nombreCompleto.isBlank()) {

                        vacios++;

                        continue;
                    }

                    if (trabajadorService
                            .existeCurp(curp)) {

                        duplicados++;

                        continue;
                    }

                    // =====================================
                    // 🔥 SEPARAR NOMBRE
                    // =====================================

                    String nombre = "";

                    String primerApellido = "";

                    String segundoApellido = "";

                    String[] partes =
                            nombreCompleto.split(" ");

                    if (partes.length > 0)
                        nombre = partes[0];

                    if (partes.length > 1)
                        primerApellido = partes[1];

                    if (partes.length > 2)
                        segundoApellido = partes[2];

                    // =====================================
                    // 🔥 TIPO TRABAJADOR
                    // =====================================

                    TipoTrabajador tipo =
                            TipoTrabajador.AMBOS;

                    try {

                        if (!tipoTexto.isBlank()) {

                            tipo =
                                    TipoTrabajador.valueOf(
                                            tipoTexto
                                                    .trim()
                                                    .toUpperCase()
                                    );
                        }

                    } catch (Exception e) {

                        tipo =
                                TipoTrabajador.AMBOS;
                    }

                    // =====================================
                    // 🔥 CREAR OBJETO
                    // =====================================

                    Trabajador trabajador =
                            new Trabajador();

                    trabajador.setNumeroPersonal(
                            numeroPersonal
                    );

                    trabajador.setCurp(curp);

                    trabajador.setNombre(nombre);

                    trabajador.setPrimerApellido(
                            primerApellido
                    );

                    trabajador.setSegundoApellido(
                            segundoApellido
                    );

                    trabajador.setAreaEmpleado(
                            areaEmpleado
                    );

                    trabajador.setEmail(email);

                    trabajador.setTipoTrabajador(
                            tipo
                    );

                    trabajador.setActivo(true);

                    // =====================================
                    // 🔥 BUSCAR PUESTO
                    // =====================================

                    if (!puestoTexto.isBlank()) {

                        Puesto puesto =
                                puestoService
                                        .buscarPorNombre(
                                                puestoTexto
                                        );

                        // 🔥 VALIDAR TIPO
                        if (puesto != null) {

                            boolean valido = false;

                            if (tipo ==
                                    TipoTrabajador.AMBOS) {

                                valido = true;
                            }

                            else if (
                                    puesto.getTipoTrabajador()
                                            ==
                                    TipoTrabajador.AMBOS) {

                                valido = true;
                            }

                            else if (
                                    puesto.getTipoTrabajador()
                                            ==
                                    tipo) {

                                valido = true;
                            }

                            if (valido) {

                                trabajador.setPuesto(
                                        puesto
                                );

                            } else {

                                System.out.println(
                                        "⚠️ Puesto incompatible fila "
                                                + i
                                );
                            }
                        }
                    }

                    // =====================================
                    // 🔥 GUARDAR
                    // =====================================

                    trabajadorService
                            .guardar(trabajador);

                    importados++;

                } catch (Exception e) {

                    System.out.println(
                            "❌ Error fila "
                                    + i
                                    + ": "
                                    + e.getMessage()
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        System.out.println(
                "✅ Importados: "
                        + importados
        );

        System.out.println(
                "⚠️ Duplicados: "
                        + duplicados
        );

        System.out.println(
                "❌ Vacíos: "
                        + vacios
        );

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
    public String ver(@PathVariable Long id, Model model){

        Trabajador trabajador = trabajadorService.buscarPorId(id).orElseThrow();

        // 🔥 CORRECTO
        List<CapacitacionTrabajador> inscripciones =
                capacitacionTrabajadorRepository
                .findByTrabajador_IdAndActivoTrue(trabajador.getId());

        Map<String, EvaluacionCapacitacion> evaluaciones = new HashMap<>();

        for(CapacitacionTrabajador ins : inscripciones){

            EvaluacionCapacitacion ev =
                    evaluacionService.buscarPorTrabajadorYCapacitacion(
                            trabajador.getId(),
                            ins.getCapacitacion().getId()
                    );

            if(ev != null){
                String key = trabajador.getId() + "-" + ins.getCapacitacion().getId();
                evaluaciones.put(key, ev);
            }
        }

        model.addAttribute("trabajador", trabajador);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("evaluaciones", evaluaciones);

        System.out.println("INSCRIPCIONES: " + inscripciones.size());

        return "trabajadores/detalle";
    }
    /* ===============================
       MOSTRAR FOTO
       =============================== */

    @GetMapping("/foto/{foto}")
    @ResponseBody
    public ResponseEntity<byte[]> verFoto(
            @PathVariable String foto) {

        try {

            // =====================================
            // 🔥 RUTA REAL PROYECTO
            // =====================================

            String basePath =
                    System.getProperty("user.dir");

            // =====================================
            // 🔥 RUTA COMPLETA
            // =====================================

            Path ruta = Paths.get(

                    basePath,

                    "uploads",

                    "trabajadores",

                    foto
            );

            System.out.println(
                    "BUSCANDO FOTO EN: "
                    + ruta.toAbsolutePath()
            );

            // =====================================
            // 🔥 VALIDAR
            // =====================================

            if (!Files.exists(ruta)) {

                System.out.println(
                        "NO EXISTE FOTO"
                );

                return ResponseEntity
                        .notFound()
                        .build();
            }

            // =====================================
            // 🔥 LEER IMAGEN
            // =====================================

            byte[] imagen =
                    Files.readAllBytes(ruta);

            // =====================================
            // 🔥 RETORNAR
            // =====================================

            return ResponseEntity.ok()

                    .contentType(
                            MediaType.IMAGE_JPEG
                    )

                    .body(imagen);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
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
        model.addAttribute("areas",areaService.listar());
        return "trabajadores/form";
    }

    /* ===============================
       ACTUALIZAR
       =============================== */

    @PostMapping("/actualizar")
    public String actualizar(

            @ModelAttribute Trabajador form,

            @RequestParam(required = false)
            MultipartFile fotoArchivo

    ) throws Exception {

        // =====================================
        // 🔥 ORIGINAL
        // =====================================

        Trabajador trabajador =

                trabajadorService
                .buscarPorId(form.getId())
                .orElseThrow();

        // =====================================
        // 🔥 DATOS
        // =====================================

        trabajador.setNumeroPersonal(
                form.getNumeroPersonal()
        );

        trabajador.setNombre(
                form.getNombre()
        );

        trabajador.setPrimerApellido(
                form.getPrimerApellido()
        );

        trabajador.setSegundoApellido(
                form.getSegundoApellido()
        );

        trabajador.setCurp(
                form.getCurp()
        );

        trabajador.setEmail(
                form.getEmail()
        );

        trabajador.setAreaEmpleado(
                form.getAreaEmpleado()
        );

        trabajador.setTipoTrabajador(
                form.getTipoTrabajador()
        );

        // =====================================
        // 🔥 PUESTO REAL
        // =====================================

        if(form.getPuesto() != null
           &&
           form.getPuesto().getId() != null){

            Puesto puesto =

                    puestoService
                    .buscarPorId(
                            form.getPuesto().getId()
                    )
                    .orElseThrow();

            trabajador.setPuesto(puesto);
        }

        // =====================================
        // 🔥 FOTO
        // =====================================

        if (fotoArchivo != null
            &&
            !fotoArchivo.isEmpty()) {

            BufferedImage imagenOriginal =
                    ImageIO.read(
                            fotoArchivo.getInputStream()
                    );

            int ancho = 300;
            int alto = 300;

            BufferedImage imagenRedimensionada =
                    new BufferedImage(
                            ancho,
                            alto,
                            BufferedImage.TYPE_INT_RGB
                    );

            Graphics2D g =
                    imagenRedimensionada
                    .createGraphics();

            g.drawImage(
                    imagenOriginal,
                    0,
                    0,
                    ancho,
                    alto,
                    null
            );

            g.dispose();

            String nombreArchivo =

                    trabajador.getNumeroPersonal()
                    +
                    ".jpg";

            Path ruta = Paths.get(
                    rutaFotos,
                    nombreArchivo
            );

            Files.createDirectories(
                    ruta.getParent()
            );

            ImageIO.write(
                    imagenRedimensionada,
                    "jpg",
                    ruta.toFile()
            );

            trabajador.setFoto(
                    nombreArchivo
            );
        }

        // =====================================
        // 🔥 ACTIVO
        // =====================================

        trabajador.setActivo(true);

        // =====================================
        // 🔥 SAVE
        // =====================================

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
    
    @GetMapping("/plantilla")
    public void descargarPlantilla(
            HttpServletResponse response) {

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Trabajadores"
                    );

            // =====================================
            // 🔥 ENCABEZADOS
            // =====================================

            Row header = sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("numeroPersonal");

            header.createCell(1)
                    .setCellValue("curp");

            header.createCell(2)
                    .setCellValue("nombreCompleto");

            header.createCell(3)
                    .setCellValue("areaEmpleado");

            header.createCell(4)
                    .setCellValue("puesto");

            header.createCell(5)
                    .setCellValue("tipoTrabajador");

            header.createCell(6)
                    .setCellValue("email");

            // =====================================
            // 🔥 FILA EJEMPLO
            // =====================================

            Row ejemplo = sheet.createRow(1);

            ejemplo.createCell(0)
                    .setCellValue("001");

            ejemplo.createCell(1)
                    .setCellValue("CURP123456789012");

            ejemplo.createCell(2)
                    .setCellValue("Juan Perez Lopez");

            ejemplo.createCell(3)
                    .setCellValue("Produccion");

            ejemplo.createCell(4)
                    .setCellValue("Operador de Producción");

            ejemplo.createCell(5)
                    .setCellValue("HOURLY");

            ejemplo.createCell(6)
                    .setCellValue("juan@mail.com");

            // =====================================
            // 🔥 VALIDACIONES
            // =====================================

            DataValidationHelper helper =
                    sheet.getDataValidationHelper();

            // =====================================
            // 🔥 TIPO TRABAJADOR
            // =====================================

            DataValidationConstraint tipoConstraint =
                    helper.createExplicitListConstraint(
                            new String[]{
                                    "HOURLY",
                                    "SALARY",
                                    "AMBOS"
                            }
                    );

            CellRangeAddressList tipoRange =
                    new CellRangeAddressList(
                            1,
                            1000,
                            5,
                            5
                    );

            DataValidation tipoValidation =
                    helper.createValidation(
                            tipoConstraint,
                            tipoRange
                    );

            tipoValidation.setShowErrorBox(true);

            tipoValidation.setSuppressDropDownArrow(false);

            sheet.addValidationData(tipoValidation);

            // =====================================
            // 🔥 HOJA OCULTA PUESTOS
            // =====================================

            Sheet hiddenSheet =
                    workbook.createSheet(
                            "PUESTOS"
                    );

            List<Puesto> puestos =
                    puestoService.listarActivos();

            for (int i = 0;
                 i < puestos.size();
                 i++) {

                Row rowHidden =
                        hiddenSheet.createRow(i);

                rowHidden.createCell(0)
                        .setCellValue(
                                puestos.get(i)
                                        .getNombrePuesto()
                        );
            }

            // 🔥 OCULTAR HOJA
            workbook.setSheetHidden(
                    workbook.getSheetIndex(hiddenSheet),
                    true
            );

            // =====================================
            // 🔥 VALIDACIÓN PUESTOS
            // =====================================

            DataValidationConstraint puestoConstraint =
                    helper.createFormulaListConstraint(
                            "PUESTOS!$A$1:$A$"
                                    + puestos.size()
                    );

            CellRangeAddressList puestoRange =
                    new CellRangeAddressList(
                            1,
                            1000,
                            4,
                            4
                    );

            DataValidation puestoValidation =
                    helper.createValidation(
                            puestoConstraint,
                            puestoRange
                    );

            puestoValidation.setShowErrorBox(true);

            puestoValidation.setSuppressDropDownArrow(false);

            sheet.addValidationData(
                    puestoValidation
            );

            // =====================================
            // 🔥 ESTILO HEADER
            // =====================================

            CellStyle headerStyle =
                    workbook.createCellStyle();

            org.apache.poi.ss.usermodel.Font font =
                    workbook.createFont();

            font.setBold(true);

            headerStyle.setFont(font);

            for (int i = 0; i <= 6; i++) {

                header.getCell(i)
                        .setCellStyle(headerStyle);
            }

            // =====================================
            // 🔥 AUTO SIZE
            // =====================================

            for (int i = 0; i <= 6; i++) {

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
                    "attachment; filename=plantilla_trabajadores.xlsx"
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
    @GetMapping("/eliminar-definitivo/{id}")
    public String eliminarDefinitivo(@PathVariable Long id, RedirectAttributes flash) {

        try {
            trabajadorService.eliminarDefinitivo(id);
            flash.addFlashAttribute("success", "Trabajador eliminado definitivamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/trabajadores";
    }

}