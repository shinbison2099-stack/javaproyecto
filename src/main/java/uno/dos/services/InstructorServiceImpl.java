package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Rol;
import uno.dos.models.entity.Instructores;
import uno.dos.repositories.InstructorRepository;

@Service
@RequiredArgsConstructor
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;

    @Override
    public Instructores guardar(Instructores instructores) {
        return instructorRepository.save(instructores);
    }

    @Override
    public List<Instructores> listarTodos() {
        return instructorRepository.findAll();
    }

    @Override
    public Optional<Instructores> buscarPorId(Long id) {
        return instructorRepository.findById(id);
    }

    @Override
    public void eliminar(Long id) {

        Instructores instructor = instructorRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

        instructor.setActivo(false);

        instructorRepository.save(instructor);
    }

    @Override
    public List<Instructores> listarInstructores() {
        return instructorRepository.findByRol(Rol.INSTRUCTOR);
    }
    
    public Optional<Instructores> buscarPorEmail(String email) {
        return instructorRepository.findByEmail(email);
    }

    @Override
    public boolean emailExiste(String email, Long id) {

        Optional<Instructores> instructor = instructorRepository.findByEmail(email);

        if(instructor.isPresent()) {
            return !instructor.get().getId().equals(id);
        }

        return false;
    }

    @Override
    public boolean existeEmail(String email) {
        return instructorRepository.existsByEmail(email);
    }
    
    public List<Instructores> listarActivos(){
        return instructorRepository.findByActivoTrueOrderByNombreAsc();
    }

    public List<Instructores> listarEliminados(){
        return instructorRepository.findByActivoFalse();
    }
	
    public Instructores buscarPorNombre(String nombre){

        return instructorRepository
                .findByNombre(nombre)
                .orElseThrow();

    }
    
    @Override
    public void eliminarDefinitivo(Long id){

        instructorRepository.deleteById(id);
    }
    
    
}