package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.Tema;

public interface TemaService {

    Tema guardar(Tema tema);

    List<Tema> listarPorCurso(Long cursoId);

    double horasUsadas(Long cursoId);

    double horasRestantes(Long cursoId);

}