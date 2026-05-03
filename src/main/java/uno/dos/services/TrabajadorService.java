package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.Trabajador;

public interface TrabajadorService {

	List<Trabajador> listarActivos();

    List<Trabajador> listarEliminados();

    Optional<Trabajador> buscarPorId(Long id);

    Trabajador guardar(Trabajador trabajador);

    void eliminar(Long id);
    
    public boolean existeCurp(String curp);

	Trabajador findById(Long id);
	
	List<Trabajador> buscarPorIds(List<Long> ids);
	
	public List<Trabajador> disponiblesParaCapacitacion(Long capId);
	
	public void eliminarDefinitivo(Long id);
    
}