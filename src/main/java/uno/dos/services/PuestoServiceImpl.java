package uno.dos.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.Puesto;
import uno.dos.models.entity.PuestoCapacitacion;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.EvaluacionCapacitacionRepository;
import uno.dos.repositories.PuestoCapacitacionRepository;
import uno.dos.repositories.PuestoRepository;
import uno.dos.repositories.TrabajadorRepository;


@Service
@RequiredArgsConstructor
public class PuestoServiceImpl implements PuestoService{

    private final PuestoRepository puestoRepository;
    private final PuestoCapacitacionRepository puestoCapacitacionRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final EvaluacionCapacitacionRepository evaluacionRepository;
    
    
    
    public List<Puesto> listarActivos(){
        return puestoRepository.findByActivoTrue();
    }

    public List<Puesto> listarEliminados(){
        return puestoRepository.findByActivoFalse();
    }

    public Optional<Puesto> buscarPorId(Long id){
        return puestoRepository.findById(id);
    }

    public void guardar(Puesto puesto){
        puestoRepository.save(puesto);
    }

    public void eliminar(Long id){

        Puesto p = puestoRepository.findById(id).orElseThrow();

        p.setActivo(false);

        puestoRepository.save(p);
    }

    public void restaurar(Long id){

        Puesto p = puestoRepository.findById(id).orElseThrow();

        p.setActivo(true);

        puestoRepository.save(p);
    }

    public boolean existeNombre(String nombre){
        return puestoRepository.existsByNombrePuesto(nombre);
    }

    public Puesto buscarPorNombre(String nombre){

        return puestoRepository
                .findByNombrePuesto(nombre)
                .orElse(null);
    }

    @Override
    public List<Puesto> listarConCapacitaciones() {

        List<Puesto> puestos = puestoRepository.findAll();

        for (Puesto p : puestos) {

            List<PuestoCapacitacion> relaciones =
                    puestoCapacitacionRepository.findByPuestoId(p.getId());

            List<Capacitacion> caps = relaciones.stream()
                    .map(pc -> pc.getCapacitacion())
                    .toList();

            p.setCapacitaciones(caps);
        }

        return puestos;
    }

    @Override
    public Map<String, Integer> progresoPorPuesto() {

        List<Trabajador> trabajadores = trabajadorRepository.findByActivoTrue();
        List<Puesto> puestos = puestoRepository.findAll();

        Map<String, Integer> progreso = new HashMap<>();

        for (Trabajador t : trabajadores) {
            for (Puesto p : puestos) {

                List<PuestoCapacitacion> caps =
                    puestoCapacitacionRepository.findByPuestoId(p.getId());

                int total = caps.size();
                int aprobados = 0;

                for (PuestoCapacitacion pc : caps) {

                    boolean aprobado = evaluacionRepository
                        .existsByTrabajadorIdAndCapacitacionIdAndAprobadaTrue(
                            t.getId(),
                            pc.getCapacitacion().getId()
                        );

                    if (aprobado) aprobados++;
                }

                int porcentaje = total == 0 ? 0 : (aprobados * 100) / total;

                progreso.put(t.getId() + "-" + p.getId(), porcentaje);
            }
        }

        return progreso;
    }
}