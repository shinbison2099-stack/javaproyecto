package uno.dos.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Tema;
import uno.dos.repositories.CursoRepository;
import uno.dos.repositories.TemaRepository;

@Service
@RequiredArgsConstructor
public class TemaServiceImpl implements TemaService {

    private final TemaRepository temaRepository;
    private final CursoRepository cursoRepository;

    @Override
    public Tema guardar(Tema tema) {

        // 🔥 calcular horas automáticamente
        if(tema.getNumeroClases() != null && tema.getHorasPorClase() != null){
            tema.setHoras(tema.getNumeroClases() * tema.getHorasPorClase());
        }

        // 🔥 validar que no exceda
        double usadas = horasUsadas(tema.getCurso().getId());

        Curso curso = cursoRepository.findById(tema.getCurso().getId()).orElseThrow();

        if(usadas + tema.getHoras() > curso.getDuracion()){
            throw new RuntimeException("Excede las horas del curso");
        }

        return temaRepository.save(tema);
    }

    @Override
    public List<Tema> listarPorCurso(Long cursoId) {
        return temaRepository.findByCurso_IdOrderByOrdenAsc(cursoId);
    }

    @Override
    public double horasUsadas(Long cursoId) {
        return temaRepository.findByCurso_IdOrderByOrdenAsc(cursoId)
                .stream()
                .mapToDouble(Tema::getHoras)
                .sum();
    }

    @Override
    public double horasRestantes(Long cursoId) {

        Curso curso = cursoRepository.findById(cursoId).orElseThrow();

        return curso.getDuracion() - horasUsadas(cursoId);
    }

	
}