package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.MatrizHabilidadDetalle;

public interface MatrizHabilidadDetalleService {

    List<MatrizHabilidadDetalle>
    buscarPorMatriz(Long matrizId);

    void guardar(
            MatrizHabilidadDetalle detalle
    );

    void eliminar(Long id);

}