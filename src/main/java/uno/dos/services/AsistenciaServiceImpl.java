package uno.dos.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Trabajador;
import uno.dos.models.entity.Asistencia;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Dispositivo;
import uno.dos.repositories.TrabajadorRepository;
import uno.dos.repositories.AsistenciaRepository;
import uno.dos.repositories.CursoRepository;
import uno.dos.repositories.DispositivoRepository;

@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final CursoRepository cursoRepository;
    private final DispositivoRepository dispositivoRepository;

    @Override
    public Asistencia registrarAsistencia(Long trabajadorId, Long cursoId, Long dispositivoId) {

        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        Dispositivo dispositivo = dispositivoRepository.findById(dispositivoId)
                .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado"));

        Asistencia asistencia = new Asistencia();
        asistencia.setTrabajador(trabajador);
        asistencia.setCurso(curso);
        asistencia.setDispositivo(dispositivo);
        asistencia.setFechaHora(LocalDateTime.now());

        return asistenciaRepository.save(asistencia);
    }

    @Override
    public List<Asistencia> listarPorCurso(Long cursoId) {
        return asistenciaRepository.findByCursoId(cursoId);
    }
}
