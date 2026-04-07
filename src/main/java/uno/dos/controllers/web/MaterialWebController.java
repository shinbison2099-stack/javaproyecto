package uno.dos.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uno.dos.models.entity.Material;
import uno.dos.models.entity.Curso;
import uno.dos.services.MaterialService;
import uno.dos.services.CursoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/materiales")
@RequiredArgsConstructor
public class MaterialWebController {

    private final MaterialService materialService;
    private final CursoService cursoService;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cursos", cursoService.listarActivos());
        return "materiales/form";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long cursoId,
                          @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }

        // Crear carpeta si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Guardar archivo
        String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path rutaCompleta = uploadPath.resolve(nombreArchivo);
        Files.copy(file.getInputStream(), rutaCompleta);

        // Buscar curso
        Curso curso = cursoService.buscarPorId(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // Crear entidad
        Material material = new Material();
        material.setCurso(curso);
        material.setNombreArchivo(nombreArchivo);
        material.setTipo(file.getContentType());
        material.setUrl(rutaCompleta.toString());
        material.setFechaSubida(LocalDateTime.now());

        materialService.guardar(material);

        return "redirect:/cursos";
    }
    
    @GetMapping("/nuevo/{cursoId}")
    public String nuevoDesdeCurso(@PathVariable Long cursoId, Model model) {

        Curso curso = cursoService.buscarPorId(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("cursos", cursoService.listarActivos());

        return "materiales/form";
    }
    
    @GetMapping("/curso/{cursoId}")
    public String verMateriales(@PathVariable Long cursoId, Model model) {

        Curso curso = cursoService.buscarPorId(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        List<Material> materiales = materialService.listarPorCurso(cursoId);

        model.addAttribute("curso", curso);
        model.addAttribute("materiales", materiales);

        return "materiales/lista";
    }
    
    
    
}