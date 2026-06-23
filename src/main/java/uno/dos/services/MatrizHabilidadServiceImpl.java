package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Curso;
import uno.dos.models.entity.Habilidad;
import uno.dos.models.entity.MatrizHabilidad;
import uno.dos.models.entity.MatrizHabilidadCurso;
import uno.dos.models.entity.MatrizHabilidadDetalle;
import uno.dos.models.entity.MatrizHabilidadTrabajador;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.models.entity.Trabajador;
import uno.dos.repositories.AreaRepository;
import uno.dos.repositories.CursoRepository;
import uno.dos.repositories.HabilidadRepository;
import uno.dos.repositories.MatrizHabilidadCursoRepository;
import uno.dos.repositories.MatrizHabilidadDetalleRepository;
import uno.dos.repositories.MatrizHabilidadRepository;
import uno.dos.repositories.MatrizHabilidadTrabajadorRepository;
import uno.dos.repositories.MatrizRepository;
import uno.dos.repositories.SubAreaRepository;
import uno.dos.repositories.TrabajadorRepository;

@Service
@RequiredArgsConstructor
public class MatrizHabilidadServiceImpl
        implements MatrizHabilidadService {

    private final MatrizHabilidadRepository matrizHabilidadRepository;

    private final AreaRepository areaRepository;

    private final SubAreaRepository subAreaRepository;

    private final HabilidadRepository habilidadRepository;

    private final CursoRepository cursoRepository;

    private final TrabajadorRepository trabajadorRepository;

    private final MatrizHabilidadDetalleRepository detalleRepository;

    private final MatrizHabilidadCursoRepository matrizCursoRepository;

    private final MatrizHabilidadTrabajadorRepository matrizTrabajadorRepository;
    
    private final TrabajadorService trabajadorService;

    @Override
    public List<MatrizHabilidad> listar(){
        return matrizHabilidadRepository.findByActivoTrue();
    }

    @Override
    public Optional<MatrizHabilidad> buscarPorId(Long id) {
        return matrizHabilidadRepository.findById(id);
    }
    
    @Override
    public void eliminar(Long id){

        MatrizHabilidad matriz =
                matrizHabilidadRepository
                .findById(id)
                .orElseThrow();

        if(!matriz.getTrabajadores().isEmpty()){

            throw new RuntimeException(
                    "No se puede eliminar la matriz porque tiene trabajadores asignados."
            );
        }

        matriz.setActivo(false);

        matrizHabilidadRepository.save(matriz);
    }
    
    
    @Transactional
    public MatrizHabilidad guardar(

            String nombre,
            Long areaId,
            Long subAreaId,
            TipoTrabajador tipoTrabajador,
            List<Long> habilidadIds,
            List<Long> cursoIds,
            List<Long> trabajadorIds) {

        MatrizHabilidad matriz =
                new MatrizHabilidad();

        matriz.setNombre(nombre);

        matriz.setArea(
                areaRepository
                        .findById(areaId)
                        .orElseThrow()
        );

        matriz.setSubArea(
                subAreaRepository
                        .findById(subAreaId)
                        .orElseThrow()
        );

        matriz.setTipoTrabajador(
                tipoTrabajador
        );

        matriz.setActivo(true);

        matriz =
                matrizHabilidadRepository
                        .save(matriz);

        // =====================
        // HABILIDADES
        // =====================

        if (habilidadIds != null) {

            for (Long id : habilidadIds) {

                Habilidad habilidad =
                        habilidadRepository
                                .findById(id)
                                .orElseThrow();

                MatrizHabilidadDetalle detalle =
                        new MatrizHabilidadDetalle();

                detalle.setMatriz(matriz);

                detalle.setHabilidad(habilidad);

                detalleRepository.save(detalle);
            }
        }

        // =====================
        // CURSOS
        // =====================

        if (cursoIds != null) {

            for (Long id : cursoIds) {

                Curso curso =
                        cursoRepository
                                .findById(id)
                                .orElseThrow();

                MatrizHabilidadCurso mc =
                        new MatrizHabilidadCurso();

                mc.setMatriz(matriz);

                mc.setCurso(curso);

                matrizCursoRepository.save(mc);
            }
        }

        // =====================
        // TRABAJADORES
        // =====================

        if (trabajadorIds != null) {

            for (Long id : trabajadorIds) {

                Trabajador trabajador =
                        trabajadorRepository
                                .findById(id)
                                .orElseThrow();

                MatrizHabilidadTrabajador mt =
                        new MatrizHabilidadTrabajador();

                mt.setMatriz(matriz);

                mt.setTrabajador(trabajador);

                matrizTrabajadorRepository.save(mt);
            }
        }

        return matriz;
    }

    @Transactional
    @Override
    public void actualizar(

            Long id,
            String nombre,
            Long areaId,
            Long subAreaId,
            TipoTrabajador tipoTrabajador,
            List<Long> habilidadIds,
            List<Long> cursoIds,
            List<Long> trabajadorIds){

        MatrizHabilidad matriz =
                matrizHabilidadRepository
                .findById(id)
                .orElseThrow();

        // ===========================
        // DATOS GENERALES
        // ===========================

        matriz.setNombre(nombre);

        matriz.setArea(
                areaRepository
                .findById(areaId)
                .orElseThrow()
        );

        matriz.setSubArea(
                subAreaRepository
                .findById(subAreaId)
                .orElseThrow()
        );

        matriz.setTipoTrabajador(
                tipoTrabajador
        );

        matrizHabilidadRepository.save(
                matriz
        );

        // ===========================
        // LIMPIAR RELACIONES
        // ===========================

        detalleRepository
                .deleteByMatrizId(id);

        matrizCursoRepository
                .deleteByMatrizId(id);

        matrizTrabajadorRepository
                .deleteByMatrizId(id);

        // ===========================
        // HABILIDADES
        // ===========================

        if(habilidadIds != null){

            for(Long habilidadId : habilidadIds){

                Habilidad habilidad =
                        habilidadRepository
                        .findById(habilidadId)
                        .orElseThrow();

                MatrizHabilidadDetalle detalle =
                        new MatrizHabilidadDetalle();

                detalle.setMatriz(
                        matriz
                );

                detalle.setHabilidad(
                        habilidad
                );

                detalleRepository.save(
                        detalle
                );
            }
        }

        // ===========================
        // CURSOS
        // ===========================

        if(cursoIds != null){

            for(Long cursoId : cursoIds){

                Curso curso =
                        cursoRepository
                        .findById(cursoId)
                        .orElseThrow();

                MatrizHabilidadCurso mc =
                        new MatrizHabilidadCurso();

                mc.setMatriz(
                        matriz
                );

                mc.setCurso(
                        curso
                );

                matrizCursoRepository
                        .save(mc);
            }
        }

        // ===========================
        // TRABAJADORES
        // ===========================

        if(trabajadorIds != null){

            for(Long trabajadorId : trabajadorIds){

                Trabajador trabajador =
                        trabajadorService
                        .buscarPorId(trabajadorId)
                        .orElseThrow();

                MatrizHabilidadTrabajador mt =
                        new MatrizHabilidadTrabajador();

                mt.setMatriz(
                        matriz
                );

                mt.setTrabajador(
                        trabajador
                );

                matrizTrabajadorRepository
                        .save(mt);
            }
        }
    }
}