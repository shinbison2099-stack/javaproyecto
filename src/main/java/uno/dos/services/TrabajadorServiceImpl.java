package uno.dos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import uno.dos.models.entity.CapacitacionTrabajador;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.CapacitacionTrabajadorRepository;
import uno.dos.repositories.TrabajadorRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrabajadorServiceImpl implements TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final CapacitacionTrabajadorRepository capacitacionTrabajadorRepository;

    @Override
    public List<Trabajador> listarActivos() {
        return trabajadorRepository.findByActivoTrue();
    }

    @Override
    public List<Trabajador> listarEliminados() {
        return trabajadorRepository.findByActivoFalse();
    }

    @Override
    public Optional<Trabajador> buscarPorId(Long id) {
        return trabajadorRepository.findById(id);
    }

    @Override
    public Trabajador guardar(Trabajador trabajador) {
        return trabajadorRepository.save(trabajador);
    }

    @Override
    public void eliminar(Long id) {

        Trabajador trabajador = trabajadorRepository.findById(id).orElseThrow();

        trabajador.setActivo(false);

        trabajadorRepository.save(trabajador);
    }

	@Override
	public boolean existeCurp(String curp) {
		// TODO Auto-generated method stub
		return trabajadorRepository.existsByCurp(curp);
	}

	@Override
	public Trabajador findById(Long id) {
	    return trabajadorRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));
	}

	@Override
	public List<Trabajador> buscarPorIds(List<Long> ids) {
	    return trabajadorRepository.findAllById(ids);
	}
	
	public List<Trabajador> disponiblesParaCapacitacion(Long capId){

	    List<Trabajador> todos = trabajadorRepository.findAll();

	    List<CapacitacionTrabajador> inscritos =
	            capacitacionTrabajadorRepository.findByCapacitacion_Id(capId);

	    Set<Long> idsInscritos = inscritos.stream()
	            .map(ct -> ct.getTrabajador().getId())
	            .collect(Collectors.toSet());

	    return todos.stream()
	            .filter(t -> !idsInscritos.contains(t.getId()))
	            .collect(Collectors.toList());
	}
	
	@Override
	public void eliminarDefinitivo(Long id) {

	    if (!trabajadorRepository.existsById(id)) {
	        throw new RuntimeException("Trabajador no encontrado");
	    }

	    trabajadorRepository.deleteById(id);
	}
	
	@Override
	public List<Trabajador> filtrarPorTipo(TipoTrabajador tipo){

	    return trabajadorRepository.findAll()
	            .stream()
	            .filter(t ->
	                tipo == TipoTrabajador.AMBOS ||
	                t.getTipoTrabajador() == tipo
	            )
	            .toList();
	}
}