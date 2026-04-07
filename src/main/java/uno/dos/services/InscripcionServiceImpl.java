package uno.dos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uno.dos.models.entity.Trabajador;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Inscripcion;
import uno.dos.repositories.TrabajadorRepository;
import uno.dos.repositories.CapacitacionRepository;
import uno.dos.repositories.CapacitacionTrabajadorRepository;
import uno.dos.repositories.CursoRepository;
import uno.dos.repositories.InscripcionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final CursoRepository cursoRepository;
    private final CapacitacionTrabajadorRepository capacitacionTrabajadorRepository;
    private final CapacitacionRepository capacitacionRepository;

    @Override
    @Transactional
    public Inscripcion inscribir(Long trabajadorId, Long cursoId) {

        if (inscripcionRepository.existsByTrabajador_IdAndCurso_Id(trabajadorId, cursoId)) {
            throw new RuntimeException("Ya inscrito");
        }

        Trabajador trabajador = trabajadorRepository.findById(trabajadorId).orElseThrow();
        Curso curso = cursoRepository.findById(cursoId).orElseThrow();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setTrabajador(trabajador);
        inscripcion.setCurso(curso);
        inscripcion.setFechaInscripcion(LocalDate.now());

        inscripcionRepository.save(inscripcion);

        // 🔥 CREAR FLUJO DE CAPACITACIONES
        int orden = 1;

        for (Capacitacion cap : curso.getCapacitaciones()) {

            CapacitacionTrabajador ct = new CapacitacionTrabajador();

            ct.setTrabajador(trabajador);
            ct.setCapacitacion(cap);
            ct.setOrden(orden++);
            ct.setActivo(true);
            ct.setAprobado(false);
            ct.setCompletado(false);

            capacitacionTrabajadorRepository.save(ct);
        }

        return inscripcion;
    }
    
    @Override
    public void eliminar(Long inscripcionId) {
        inscripcionRepository.deleteById(inscripcionId);
    }

    @Override
    public List<Inscripcion> listarPorCurso(Long cursoId) {
        return inscripcionRepository.findByCurso_Id(cursoId);
    }
    
    @Override
    public List<Inscripcion> listarTodas() {
        return inscripcionRepository.findAll();
    }
    
    @Transactional
    public void desinscribir(Long trabajadorId, Long capId){

        CapacitacionTrabajador ins = capacitacionTrabajadorRepository
            .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, capId)
            .orElseThrow();

        ins.setActivo(false);

        capacitacionTrabajadorRepository.save(ins);
    }
    
    @Transactional
    public void restaurar(Long trabajadorId, Long capId){

        CapacitacionTrabajador ins = capacitacionTrabajadorRepository
            .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, capId)
            .orElseThrow();

        ins.setActivo(true);

        capacitacionTrabajadorRepository.save(ins);
    }

	@Override
	public void restaurarInscripcion(Long trabajadorId, Long capId) {

		 CapacitacionTrabajador ins = capacitacionTrabajadorRepository
		            .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, capId)
		            .orElseThrow();

		        ins.setActivo(true);

		        capacitacionTrabajadorRepository.save(ins);
		
	}
	
	@Transactional
	public void marcarComoAprobado(Long trabajadorId, Long cursoId) {

	    Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
	            .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));

	    Curso curso = cursoRepository.findById(cursoId)
	            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

	    // 🔥 AHORA SON MUCHAS CAPACITACIONES
	    List<Capacitacion> caps = curso.getCapacitaciones();

	    for (Capacitacion cap : caps) {

	        CapacitacionTrabajador ct = capacitacionTrabajadorRepository
	                .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, cap.getId())
	                .orElse(new CapacitacionTrabajador());

	        ct.setTrabajador(trabajador);
	        ct.setCapacitacion(cap);
	        ct.setAprobado(true);
	        ct.setCalificacion(100.0);
	        ct.setNivel(3);
	        ct.setActivo(true);

	        capacitacionTrabajadorRepository.save(ct);
	    }
	}

	@Override
	public CapacitacionTrabajador siguiente(Long trabajadorId) {

	    return capacitacionTrabajadorRepository
	        .findFirstByTrabajador_IdAndCompletadoFalseOrderByOrdenAsc(trabajadorId)
	        .orElse(null); // 🔥 AQUÍ ESTÁ LA SOLUCIÓN
	}
	
	
	@Transactional
	public void calificar(Long trabajadorId, Long capId, double calificacion){

	    CapacitacionTrabajador ct = capacitacionTrabajadorRepository
	        .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, capId)
	        .orElseThrow();

	    ct.setCalificacion(calificacion);

	    if(calificacion >= 8){
	        ct.setAprobado(true);
	        ct.setCompletado(true);
	    }else{
	        ct.setAprobado(false);
	        // 🔥 NO AVANZA
	    }

	    capacitacionTrabajadorRepository.save(ct);
	}
	
	public boolean cursoCompletado(Long trabajadorId){

	    return capacitacionTrabajadorRepository
	        .findByTrabajador_IdAndCompletadoFalse(trabajadorId)
	        .isEmpty();
	}

	@Transactional
	public void asignarCapacitacion(Long trabajadorId, Long capId){

	    // 🔍 Buscar trabajador
	    Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
	            .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));

	    // 🔍 Buscar capacitación
	    Capacitacion capacitacion = capacitacionRepository.findById(capId)
	            .orElseThrow(() -> new RuntimeException("Capacitación no encontrada"));

	    // 🚫 VALIDAR SI YA EXISTE
	    Optional<CapacitacionTrabajador> existente =
	            capacitacionTrabajadorRepository
	            .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, capId);

	    if (existente.isPresent()) {
	        return; // ya está inscrito, no duplicar
	    }

	    // 🆕 CREAR RELACIÓN
	    CapacitacionTrabajador ct = new CapacitacionTrabajador();

	    ct.setTrabajador(trabajador);
	    ct.setCapacitacion(capacitacion);

	    ct.setFechaInscripcion(LocalDate.now());

	    ct.setActivo(true);
	    ct.setAprobado(false);
	    ct.setCompletado(false);

	    // opcional si usas orden
	    // ct.setOrden(1);

	    capacitacionTrabajadorRepository.save(ct);
	}
   
}