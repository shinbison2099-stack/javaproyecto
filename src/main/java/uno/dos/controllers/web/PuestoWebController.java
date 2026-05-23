package uno.dos.controllers.web;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Puesto;
import uno.dos.models.entity.PuestoCurso;
import uno.dos.models.entity.TipoTrabajador;

import uno.dos.services.CursoService;
import uno.dos.services.PuestoService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.http.HttpServletResponse;



@Controller
@RequestMapping("/puestos")
@RequiredArgsConstructor
public class PuestoWebController {

    private final PuestoService puestoService;
    private final TemplateEngine templateEngine;
    
    private final CursoService cursoService;

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
        model.addAttribute("tiposTrabajador", TipoTrabajador.values());

        // 🔥 inicialmente todos (o vacíos si prefieres)
        model.addAttribute("cursos", cursoService.listarActivos());

        return "puestos/form";
    }

    /* GUARDAR */

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Puesto puesto,
                          @RequestParam(required = false) List<Long> cursosIds){

        // 🔥 ACTIVO POR DEFAULT
        puesto.setActivo(true);

        // 🔥 LIMPIAR RELACIONES ANTERIORES (IMPORTANTE EN EDIT)
        if(puesto.getPuestoCursos() != null){
            puesto.getPuestoCursos().clear();
        }

        // 🔥 CREAR RELACIONES NUEVAS
        if(cursosIds != null){

            for(Long cursoId : cursosIds){

                Curso curso = cursoService.buscarPorId(cursoId).orElse(null);

                if(curso != null){

                    PuestoCurso pc = new PuestoCurso();
                    pc.setPuesto(puesto);
                    pc.setCurso(curso);

                    // opcional
                    pc.setObligatorio(true);

                    puesto.getPuestoCursos().add(pc);
                }
            }
        }

        puestoService.guardar(puesto);

        return "redirect:/puestos";
    }

    /* EDITAR */

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model){

        model.addAttribute("puesto",
                puestoService.buscarPorId(id).orElseThrow());
        
     // 🔥 AGREGAR ESTO
        model.addAttribute("tiposTrabajador", TipoTrabajador.values());
        model.addAttribute("cursos", cursoService.listarActivos());

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
    public String importarExcel(
            @RequestParam MultipartFile archivo){

        try(Workbook workbook =
                    WorkbookFactory.create(
                            archivo.getInputStream())){

            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            for(int i = 1;
                i <= sheet.getLastRowNum();
                i++){

                try{

                    Row row = sheet.getRow(i);

                    if(row == null) continue;

                    // =====================================
                    // 🔥 DATOS
                    // =====================================

                    String nombre =
                            formatter.formatCellValue(
                                    row.getCell(0)
                            ).trim();

                    String nivelTexto =
                            formatter.formatCellValue(
                                    row.getCell(1)
                            ).trim();

                    String tipoTexto =
                            formatter.formatCellValue(
                                    row.getCell(2)
                            ).trim();

                    // 🔥 CURSOS
                    String cursosTexto =
                            formatter.formatCellValue(
                                    row.getCell(3)
                            ).trim();

                    // =====================================
                    // 🔥 VALIDACIÓN
                    // =====================================

                    if(nombre.isBlank()){

                        continue;
                    }

                    // =====================================
                    // 🔥 NIVEL
                    // =====================================

                    Integer nivel = 0;

                    try{

                        if(!nivelTexto.isBlank()){

                            nivel =
                                    Integer.parseInt(
                                            nivelTexto
                                    );
                        }

                    }catch(Exception e){

                        nivel = 0;
                    }

                    // =====================================
                    // 🔥 TIPO
                    // =====================================

                    TipoTrabajador tipo =
                            TipoTrabajador.AMBOS;

                    try{

                        if(!tipoTexto.isBlank()){

                            tipo =
                                    TipoTrabajador.valueOf(
                                            tipoTexto
                                                    .trim()
                                                    .toUpperCase()
                                    );
                        }

                    }catch(Exception e){

                        tipo =
                                TipoTrabajador.AMBOS;
                    }

                    // =====================================
                    // 🔥 DUPLICADO
                    // =====================================

                    if(puestoService.existeNombre(
                            nombre)){

                        continue;
                    }

                    // =====================================
                    // 🔥 CREAR PUESTO
                    // =====================================

                    Puesto p = new Puesto();

                    p.setNombrePuesto(nombre);

                    p.setNivel(nivel);

                    p.setTipoTrabajador(tipo);

                    p.setActivo(true);

                    // 🔥 GUARDAR
                    Puesto guardado =
                            puestoService.guardar(p);

                    // =====================================
                    // 🔥 CURSOS
                    // =====================================

                    if(!cursosTexto.isBlank()){

                        String[] claves =
                                cursosTexto.split(",");

                        for(String clave : claves){

                            try{

                                // 🔥 LIMPIAR
                                clave =
                                        clave
                                                .trim()
                                                .toUpperCase();

                                System.out.println(
                                        "🔍 Buscando curso: ["
                                                + clave
                                                + "]"
                                );

                                // =====================================
                                // 🔥 BUSCAR CURSO
                                // =====================================

                                Curso curso =
                                        cursoService
                                                .buscarPorNombreNormalizado(
                                                        clave
                                                );

                                System.out.println(
                                        "✅ Curso encontrado: "
                                                + curso
                                );

                                // 🔥 NO EXISTE
                                if(curso == null){

                                    System.out.println(
                                            "❌ No existe curso: "
                                                    + clave
                                    );

                                    continue;
                                }

                                // =====================================
                                // 🔥 VALIDAR TIPO
                                // =====================================

                                boolean valido = false;

                                // 🔵 PUESTO AMBOS
                                if(tipo ==
                                        TipoTrabajador.AMBOS){

                                    valido = true;
                                }

                                // 🔵 CURSO AMBOS
                                else if(curso.getTipoTrabajador()
                                        ==
                                        TipoTrabajador.AMBOS){

                                    valido = true;
                                }

                                // 🔵 MISMO TIPO
                                else if(curso.getTipoTrabajador()
                                        ==
                                        tipo){

                                    valido = true;
                                }

                                // =====================================
                                // 🔥 ASIGNAR
                                // =====================================

                                if(valido){

                                    System.out.println(
                                            "🔥 Asignando curso: "
                                                    + curso.getNombreCurso()
                                    );

                                    puestoService
                                            .asignarCurso(
                                                    guardado.getId(),
                                                    curso.getId()
                                            );

                                }else{

                                    System.out.println(
                                            "⚠️ Curso incompatible: "
                                                    + curso.getNombreCurso()
                                    );
                                }

                            }catch(Exception e){

                                System.out.println(
                                        "❌ Error curso fila "
                                                + i
                                                + ": "
                                                + e.getMessage()
                                );
                            }
                        }
                    }

                }catch(Exception e){

                    System.out.println(
                            "❌ Error fila "
                                    + i
                                    + ": "
                                    + e.getMessage()
                    );
                }
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
    
    @GetMapping("/plantilla")
    public void descargarPlantilla(
            HttpServletResponse response){

        try(Workbook workbook =
                    new XSSFWorkbook()){

            Sheet sheet =
                    workbook.createSheet(
                            "Plantilla Puestos"
                    );

            // =====================================
            // 🔥 ESTILO HEADER
            // =====================================

            CellStyle headerStyle =
                    workbook.createCellStyle();

            Font headerFont =
                    workbook.createFont();

            headerFont.setBold(true);

            headerStyle.setFont(headerFont);

            // =====================================
            // 🔥 ENCABEZADOS
            // =====================================

            Row header = sheet.createRow(0);

            String[] columnas = {
                    "nombrePuesto",
                    "nivel",
                    "tipoTrabajador",
                    "cursos"
            };

            for(int i = 0;
                i < columnas.length;
                i++){

                Cell cell =
                        header.createCell(i);

                cell.setCellValue(
                        columnas[i]
                );

                cell.setCellStyle(
                        headerStyle
                );
            }

            // =====================================
            // 🔥 EJEMPLOS
            // =====================================

            Row ejemplo1 = sheet.createRow(1);

            ejemplo1.createCell(0)
                    .setCellValue(
                            "Tecnico Mantenimiento"
                    );

            ejemplo1.createCell(1)
                    .setCellValue(1);

            ejemplo1.createCell(2)
                    .setCellValue("HOURLY");

            ejemplo1.createCell(3)
                    .setCellValue(
                            "CUR001,CUR002,CUR003"
                    );

            // =====================================

            Row ejemplo2 = sheet.createRow(2);

            ejemplo2.createCell(0)
                    .setCellValue(
                            "Supervisor Produccion"
                    );

            ejemplo2.createCell(1)
                    .setCellValue(2);

            ejemplo2.createCell(2)
                    .setCellValue("SALARY");

            ejemplo2.createCell(3)
                    .setCellValue(
                            "CUR010,CUR011"
                    );

            // =====================================

            Row ejemplo3 = sheet.createRow(3);

            ejemplo3.createCell(0)
                    .setCellValue(
                            "Gerente Planta"
                    );

            ejemplo3.createCell(1)
                    .setCellValue(3);

            ejemplo3.createCell(2)
                    .setCellValue("AMBOS");

            ejemplo3.createCell(3)
                    .setCellValue(
                            "CUR001,CUR020"
                    );

            // =====================================
            // 🔥 VALIDACIÓN
            // =====================================

            DataValidationHelper helper =
                    sheet.getDataValidationHelper();

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
                            2,
                            2
                    );

            DataValidation validation =
                    helper.createValidation(
                            tipoConstraint,
                            tipoRange
                    );

            validation.setShowErrorBox(true);

            validation.setSuppressDropDownArrow(
                    false
            );

            sheet.addValidationData(validation);

            // =====================================
            // 🔥 AUTO SIZE
            // =====================================

            for(int i = 0;
                i < columnas.length;
                i++){

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
                    "attachment; filename=plantilla_puestos.xlsx"
            );

            workbook.write(
                    response.getOutputStream()
            );

            response.getOutputStream().flush();

        }catch(Exception e){

            e.printStackTrace();
        }
    }
    
    @PostMapping("/eliminar-masivo")
    public String eliminarMasivo(@RequestParam("ids") List<Long> ids,
                                RedirectAttributes flash) {

        try {
            puestoService.eliminarMasivoDefinitivo(ids);
            flash.addFlashAttribute("success", "Puestos eliminados definitivamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/puestos/eliminados";
    }
    
    @GetMapping("/cursos-por-tipo/{tipo}")
    @ResponseBody
    public List<Curso> cursosPorTipo(@PathVariable TipoTrabajador tipo){
        return cursoService.filtrarPorTipo(tipo);
    }
    

}