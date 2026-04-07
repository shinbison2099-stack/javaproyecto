package uno.dos.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import uno.dos.models.entity.Puesto;

public interface PuestoService {
	
	   public List<Puesto> listarActivos();
       public List<Puesto> listarEliminados();
       public Optional<Puesto> buscarPorId(Long id);
       public void guardar(Puesto puesto);
       public void eliminar(Long id);
	   public void restaurar(Long id);
	   public boolean existeNombre(String nombre);
	   Map<String, Integer> progresoPorPuesto();
	   public Puesto buscarPorNombre(String nombre);	
	   public List<Puesto> listarConCapacitaciones();

}
