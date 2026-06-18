package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.MatrizHabilidadTrabajador;

public interface MatrizHabilidadTrabajadorService {

    List<MatrizHabilidadTrabajador>
    buscarPorMatriz(Long matrizId);

    void guardar(
            MatrizHabilidadTrabajador trabajador
    );

    void eliminar(Long id);

}