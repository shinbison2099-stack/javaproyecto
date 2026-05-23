package uno.dos.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.EvaluacionCapacitacion;
import uno.dos.models.entity.Matriz;
import uno.dos.models.entity.Puesto;
import uno.dos.models.entity.SkillMatrix;

public interface MatrizService {

    List<Map<String, Object>> matrizCarrera(Long trabajadorId);
    public boolean puestoCompletado(Long trabajadorId, Long puestoId);
    public List<Capacitacion> getCapacitacionesPorPuesto(Long puestoId);
    public Puesto getSiguientePuesto(Puesto actual);
    // 🔥 AGREGA ESTE
    Map<String, Integer> matrizCapacitaciones();
    public Map<String, Integer> progresoPorPuesto();
    public Map<String, Integer> matrizCursos();
    Map<String, EvaluacionCapacitacion> mapaEvaluaciones();
    public int calcularEstadoCurso(Long trabajadorId, Curso curso);
    
    Matriz guardar(Matriz matriz);
    List<Matriz> listar();
    Optional<Matriz> buscarPorId(Long id);
    void eliminar(Long id);
    public int calcularPorcentajeCurso(Long trabajadorId, Curso curso);
    public Map<String, Integer> matrizCursosPorcentaje();
    List<SkillMatrix> listarSkills();
    
}