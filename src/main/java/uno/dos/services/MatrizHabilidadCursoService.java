package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.MatrizHabilidadCurso;

public interface MatrizHabilidadCursoService {

    List<MatrizHabilidadCurso>
    buscarPorMatriz(Long matrizId);

    void guardar(
            MatrizHabilidadCurso curso
    );

    void eliminar(Long id);

}