package uno.dos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

import uno.dos.models.entity.*;
import uno.dos.repositories.*;

@Service
@RequiredArgsConstructor
public class MatrizServiceImpl implements MatrizService {

    private final PuestoRepository puestoRepository;
    private final PuestoCapacitacionRepository puestoCapacitacionRepository;
    private final EvaluacionCapacitacionRepository evaluacionRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CursoRepository cursoRepository;
    private final CapacitacionTrabajadorRepository capacitacionTrabajadorRepository;
    private final MatrizRepository matrizRepository;

    // 🔥 MATRIZ PLAN DE CARRERA
    @Override
    public List<Map<String, Object>> matrizCarrera(Long trabajadorId){

        List<Puesto> puestos = puestoRepository.findAllByOrderByNivelAsc();
        List<Map<String, Object>> matriz = new ArrayList<>();

        for(Puesto p : puestos){

            Map<String, Object> fila = new HashMap<>();
            fila.put("puesto", p.getNombrePuesto());

            List<PuestoCapacitacion> cursos =
                    puestoCapacitacionRepository.findByPuestoId(p.getId());

            int total = cursos.size();
            int aprobados = 0;

            for(PuestoCapacitacion pc : cursos){

                boolean aprobado = evaluacionRepository
                        .existsByTrabajador_IdAndCapacitacion_IdAndAprobadaTrue(
                                trabajadorId,
                                pc.getCapacitacion().getId()
                        );

                if(aprobado) aprobados++;
            }

            fila.put("total", total);
            fila.put("aprobados", aprobados);
            fila.put("completo", total > 0 && total == aprobados);

            matriz.add(fila);
        }

        return matriz;
    }

    // 🔥 MATRIZ DE CAPACITACIONES (COLORES)
    @Override
    public Map<String, Integer> matrizCapacitaciones(){

        List<Trabajador> trabajadores = trabajadorRepository.findByActivoTrue();
        List<Capacitacion> caps = capacitacionRepository.listarActivas();

        Map<String, Integer> matriz = new HashMap<>();

        for(Trabajador t : trabajadores){
            for(Capacitacion c : caps){

                // ✅ aprobado (correcto)
                boolean aprobado = evaluacionRepository
                        .existsByTrabajador_IdAndCapacitacion_IdAndAprobadaTrue(
                                t.getId(),
                                c.getId()
                        );

                // 🔥 AQUÍ EL FIX REAL
                boolean inscrito = inscripcionRepository.findByTrabajador_Id(t.getId())
                        .stream()
                        .anyMatch(ins -> 
                            ins.getCurso().getCapacitaciones()
                               .stream()
                               .anyMatch(cap -> cap.getId().equals(c.getId()))
                        );

                int valor = 0;

                if(aprobado){
                    valor = 3; // 🟢 verde
                } else if(inscrito){
                    valor = 1; // 🟡 amarillo
                }

                matriz.put(t.getId() + "-" + c.getId(), valor);
            }
        }

        return matriz;
    }

    // 🔥 VALIDAR SI COMPLETÓ PUESTO
    @Override
    public boolean puestoCompletado(Long trabajadorId, Long puestoId) {

        List<PuestoCapacitacion> lista =
                puestoCapacitacionRepository.findByPuestoId(puestoId);

        for (PuestoCapacitacion pc : lista) {

            boolean aprobado = evaluacionRepository
                    .existsByTrabajador_IdAndCapacitacion_IdAndAprobadaTrue(
                            trabajadorId,
                            pc.getCapacitacion().getId()
                    );

            if (!aprobado) {
                return false;
            }
        }

        return true;
    }

    // 🔥 CAPACITACIONES POR PUESTO
    @Override
    public List<Capacitacion> getCapacitacionesPorPuesto(Long puestoId) {

        return puestoCapacitacionRepository
                .findByPuestoId(puestoId)
                .stream()
                .map(PuestoCapacitacion::getCapacitacion)
                .toList();
    }

    // 🔥 SIGUIENTE PUESTO
    @Override
    public Puesto getSiguientePuesto(Puesto actual) {

        return puestoRepository
                .findAllByOrderByNivelAsc()
                .stream()
                .filter(p -> p.getNivel() > actual.getNivel())
                .findFirst()
                .orElse(null);
    }

    // 🔥 PROGRESO POR PUESTO (%)
    @Override
    public Map<String, Integer> progresoPorPuesto(){

        List<Trabajador> trabajadores = trabajadorRepository.findByActivoTrue();
        List<Puesto> puestos = puestoRepository.findAll();

        Map<String, Integer> progreso = new HashMap<>();

        for(Trabajador t : trabajadores){
            for(Puesto p : puestos){

                List<PuestoCapacitacion> caps =
                        puestoCapacitacionRepository.findByPuestoId(p.getId());

                int total = caps.size();
                int aprobados = 0;

                for(PuestoCapacitacion pc : caps){

                    boolean aprobado = evaluacionRepository
                            .existsByTrabajador_IdAndCapacitacion_IdAndAprobadaTrue(
                                    t.getId(),
                                    pc.getCapacitacion().getId()
                            );

                    if(aprobado) aprobados++;
                }

                int porcentaje = (total == 0) ? 0 : (aprobados * 100 / total);

                progreso.put(t.getId() + "-" + p.getId(), porcentaje);
            }
        }

        return progreso;
    }

    @Override
    public Map<String, Integer> matrizCursos(){

        List<Trabajador> trabajadores = trabajadorRepository.findByActivoTrue();
        List<Curso> cursos = cursoRepository.findAll();

        Map<String, Integer> matriz = new HashMap<>();

        for(Trabajador t : trabajadores){
            for(Curso c : cursos){

                int valor = calcularEstadoCurso(t.getId(), c);

                matriz.put(t.getId() + "-" + c.getId(), valor);
            }
        }

        return matriz;
    }
    
    @Override
    public Map<String, EvaluacionCapacitacion> mapaEvaluaciones(){

        List<EvaluacionCapacitacion> evaluaciones = evaluacionRepository.findAll();

        Map<String, EvaluacionCapacitacion> mapa = new HashMap<>();

        for(EvaluacionCapacitacion e : evaluaciones){

            String key = e.getTrabajador().getId() + "-" + e.getCapacitacion().getId();

            mapa.put(key, e);
        }

        return mapa;
    }
    
    public int calcularEstadoCurso(Long trabajadorId, Curso curso){

        List<Capacitacion> caps = curso.getCapacitaciones();

        int total = caps.size();
        int aprobadas = 0;

        for(Capacitacion cap : caps){

            boolean aprobado = capacitacionTrabajadorRepository
                    .findByTrabajador_IdAndCapacitacion_Id(trabajadorId, cap.getId())
                    .map(ct -> Boolean.TRUE.equals(ct.getAprobado()))
                    .orElse(false);

            if(aprobado){
                aprobadas++;
            }
        }

        if(aprobadas == total && total > 0){
            return 3; // 🟢 completo
        } else if(aprobadas > 0){
            return 1; // 🟡 en progreso
        } else {
            return 0; // 🔴 no iniciado
        }
    }

    @Override
    public Matriz guardar(Matriz matriz) {

        // 🔥 Validación básica
        if(matriz.getNombre() == null || matriz.getNombre().isEmpty()){
            throw new RuntimeException("El nombre de la matriz es obligatorio");
        }

        if(matrizRepository.findByNombre(matriz.getNombre()).isPresent()){
            throw new RuntimeException("Ya existe una matriz con ese nombre");
        }
        
        return matrizRepository.save(matriz);
    }

    @Override
    public List<Matriz> listar() {
        return matrizRepository.findAll();
    }

    @Override
    public Optional<Matriz> buscarPorId(Long id) {
        return matrizRepository.findById(id);
    }

    
    
    
}