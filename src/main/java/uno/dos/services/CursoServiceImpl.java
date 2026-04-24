package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.Curso;
import uno.dos.repositories.CapacitacionRepository;
import uno.dos.repositories.CursoRepository;

@Service
@RequiredArgsConstructor
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final CapacitacionRepository capacitacionRepository;
    @Override
    public Curso guardar(Curso curso) {
        return cursoRepository.save(curso);
    }

    @Override
    public List<Curso> listarActivos(){
        return cursoRepository.findActivosConCapacitaciones();
    }

    @Override
    public Optional<Curso> buscarPorId(Long id) {
        return cursoRepository.findById(id);
    }

        
    @Override
    public boolean existeClaveCurso(String claveCurso) {
        return cursoRepository.existsByClaveCurso(claveCurso);
    }
    
    

    public List<Curso> listarEliminados(){
        return cursoRepository.findByActivoFalse();
    }

    

    public void eliminar(Long id){

        Curso curso = cursoRepository
                .findById(id)
                .orElseThrow();

        curso.setActivo(false);

        cursoRepository.save(curso);
    }
    
    @Transactional
    public void asignarCapacitaciones(Long cursoId, List<Long> capacitacionIds){

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        List<Capacitacion> caps = capacitacionRepository.findAllById(capacitacionIds);

        for(Capacitacion cap : caps){
            cap.setCurso(curso); // 🔥 AQUÍ SE HACE LA RELACIÓN
        }

        capacitacionRepository.saveAll(caps);
    }
    
    
    public List<Curso> listarConCapacitaciones(){
        return cursoRepository.listarConCapacitaciones();
    }
    
    public int horasUsadas(Long cursoId){

        Curso curso = cursoRepository.findById(cursoId).orElseThrow();

        return curso.getCapacitaciones()
                .stream()
                .mapToInt(cap -> cap.getDuracionHoras() != null ? cap.getDuracionHoras() : 0)
                .sum();
    }
    
    public int horasRestantes(Long cursoId){

        Curso curso = cursoRepository.findById(cursoId).orElseThrow();

        int usadas = horasUsadas(cursoId);

        return curso.getDuracion() - usadas;
    }

    @Override
    public List<Curso> buscarPorIds(List<Long> ids) {
        return cursoRepository.findAllById(ids);
    }
    
    @Transactional
    public void eliminarDefinitivo(Long id){

        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // 🔥 romper relación
        curso.getPuestos().forEach(p -> p.getCursos().remove(curso));
        curso.getPuestos().clear();

        cursoRepository.save(curso);

        // 🔥 eliminar
        cursoRepository.delete(curso);
    }
    
}