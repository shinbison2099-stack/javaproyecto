package uno.dos.services;

import java.util.List;

import uno.dos.models.entity.CategoriaPuesto;

public interface CategoriaPuestoService {

    List<CategoriaPuesto> listar();

    CategoriaPuesto guardar(CategoriaPuesto categoria);

}