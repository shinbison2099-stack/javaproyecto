package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Inscripcion;

public interface InscripcionService {

    Inscripcion inscribir(Long trabajadorId, Long cursoId);

    List<Inscripcion> listarPorCurso(Long cursoId);
    
    void eliminar(Long inscripcionId);
    
    List<Inscripcion> listarTodas();
    
    void desinscribir(Long trabajadorId, Long capacitacionId);
    
    public void restaurarInscripcion(Long trabajadorId, Long capId);
    
    public CapacitacionTrabajador siguiente(Long trabajadorId);
    
    public void calificar(Long trabajadorId, Long capId, double calificacion);

    public void asignarCapacitacion(Long trabajadorId, Long capId);
}