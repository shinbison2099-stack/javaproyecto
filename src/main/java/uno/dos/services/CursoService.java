package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.Curso;

public interface CursoService {

    Curso guardar(Curso curso);

    List<Curso> listarActivos();

    Optional<Curso> buscarPorId(Long id);

    void eliminar(Long id);
    
    boolean existeClaveCurso(String claveCurso);
    
    public List<Curso> listarEliminados();
       
    public void asignarCapacitaciones(Long cursoId, List<Long> capacitacionIds);    
    
    public List<Curso> listarConCapacitaciones();
    
    public int horasUsadas(Long cursoId);
    
    public int horasRestantes(Long cursoId);
}