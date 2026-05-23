package uno.dos.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Puesto;
import uno.dos.models.entity.PuestoCapacitacion;
import uno.dos.models.entity.PuestoCurso;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.CursoRepository;
import uno.dos.repositories.EvaluacionCapacitacionRepository;
import uno.dos.repositories.PuestoCapacitacionRepository;
import uno.dos.repositories.PuestoCursoRepository;
import uno.dos.repositories.PuestoRepository;
import uno.dos.repositories.TrabajadorRepository;


@Service
@RequiredArgsConstructor
public class PuestoServiceImpl implements PuestoService{

    private final PuestoRepository puestoRepository;
    private final PuestoCapacitacionRepository puestoCapacitacionRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final EvaluacionCapacitacionRepository evaluacionRepository;
    private final CursoRepository cursoRepository;
    private final PuestoCursoRepository puestoCursoRepository;
    
    
    
    
    public List<Puesto> listarActivos(){
        return puestoRepository.findByActivoTrue();
    }

    public List<Puesto> listarEliminados(){
        return puestoRepository.findByActivoFalse();
    }

    public Optional<Puesto> buscarPorId(Long id){
        return puestoRepository.findById(id);
    }

    @Override
    public Puesto guardar(Puesto puesto){
       return puestoRepository.save(puesto);
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

        List<Long> ids = puestos.stream()
                .map(Puesto::getId)
                .toList();

        List<PuestoCapacitacion> relaciones =
                puestoCapacitacionRepository.findAllByPuestoIds(ids);

        Map<Long, List<Capacitacion>> mapa = relaciones.stream()
                .collect(Collectors.groupingBy(
                        pc -> pc.getPuesto().getId(),
                        Collectors.mapping(PuestoCapacitacion::getCapacitacion, Collectors.toList())
                ));

        for (Puesto p : puestos) {
            p.setCapacitaciones(mapa.getOrDefault(p.getId(), List.of()));
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
    
    @Override
    public void eliminarMasivoDefinitivo(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("No se seleccionaron registros");
        }

        // validar existencia (opcional pero pro)
        for (Long id : ids) {
            if (!puestoRepository.existsById(id)) {
                throw new RuntimeException("Puesto no encontrado: " + id);
            }
        }

        // 🔥 eliminación masiva real
        puestoRepository.deleteAllByIdInBatch(ids);
    }

	
    @Override
    @Transactional
    public void asignarCurso(
            Long puestoId,
            Long cursoId){

        Puesto puesto =
                puestoRepository
                        .findById(puestoId)
                        .orElseThrow();

        Curso curso =
                cursoRepository
                        .findById(cursoId)
                        .orElseThrow();

        // =====================================
        // 🔥 VALIDAR DUPLICADO
        // =====================================

        boolean existe =
                puestoCursoRepository
                        .existsByPuestoIdAndCursoId(
                                puestoId,
                                cursoId
                        );

        if(existe){

            return;
        }

        // =====================================
        // 🔥 CREAR RELACIÓN
        // =====================================

        PuestoCurso pc =
                new PuestoCurso();

        pc.setPuesto(puesto);

        pc.setCurso(curso);

        pc.setObligatorio(true);

        pc.setOrden(1);

        // =====================================
        // 🔥 GUARDAR DIRECTO
        // =====================================

        puestoCursoRepository.save(pc);
    }
}