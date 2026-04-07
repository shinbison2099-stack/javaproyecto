package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.Asistencia;

public interface AsistenciaService {

    Asistencia registrarAsistencia(Long trabajadorId, Long cursoId, Long dispositivoId);

    List<Asistencia> listarPorCurso(Long cursoId);

}