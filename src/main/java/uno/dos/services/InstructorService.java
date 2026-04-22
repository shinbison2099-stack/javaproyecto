package uno.dos.services;

import java.util.List;
import java.util.Optional;

import uno.dos.models.entity.Instructores;

public interface InstructorService {

    Instructores guardar(Instructores instructores);

    List<Instructores> listarTodos();

    Optional<Instructores> buscarPorId(Long id);

    void eliminar(Long id);

    List<Instructores> listarInstructores();
    
    public Optional<Instructores> buscarPorEmail(String email);
    
    public boolean emailExiste(String email, Long id);

	boolean existeEmail(String email);
    
	public List<Instructores> listarActivos();

	public List<Instructores> listarEliminados();
	
	public Instructores buscarPorNombre(String nombre);
	
	public void eliminarDefinitivo(Long id);

}