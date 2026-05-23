package uno.dos.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.models.entity.TipoTrabajador;
import uno.dos.repositories.CapacitacionRepository;

@Service
@RequiredArgsConstructor
public class CapacitacionServiceImpl implements CapacitacionService {

    private final CapacitacionRepository capacitacionRepository;

   
    @Override
    public List<Capacitacion> listarActivas(){
        return capacitacionRepository.listarActivas();
    }
    @Override
    public Optional<Capacitacion> buscarPorId(Long id) {
        return capacitacionRepository.findById(id);
    }

    @Override
    public Capacitacion guardar(Capacitacion capacitacion) {
        return capacitacionRepository.save(capacitacion);
    }

    @Override
    public void eliminar(Long id) {

        Capacitacion cap =
                capacitacionRepository.findById(id).orElseThrow();

        cap.setActivo(false);

        capacitacionRepository.save(cap);
    }

        public List<Capacitacion> listarEliminadas(){

        return capacitacionRepository.findByActivoFalse();
    }

        public boolean existeClave(String clave){

            return capacitacionRepository.existsByClaveCapacitacion(clave);

        }
        
        @Transactional
        @Override
        public void restaurarCapacitacion(Long id){

            Capacitacion cap = capacitacionRepository.findById(id).orElseThrow();

            cap.setActivo(Boolean.TRUE); // 🔥 IMPORTANTE

            capacitacionRepository.save(cap);
        }
        @Override
        public List<Capacitacion> buscarPorIds(List<Long> ids) {
            return capacitacionRepository.findAllById(ids);
        }
        
        @Override
        public List<Capacitacion> buscarPorCursoId(Long cursoId) {
            return capacitacionRepository.findByCursoIdAndActivoTrue(cursoId);
        }
        
        @Override
        public List<Capacitacion> buscarPorCursoIds(List<Long> cursoIds) {
            return capacitacionRepository
                    .findByCursoIdInAndActivoTrue(cursoIds)
                    .stream()
                    .distinct()
                    .toList();
        }
        
        @Override
        public void eliminarDefinitivo(Long id) {

            if (!capacitacionRepository.existsById(id)) {
                throw new RuntimeException("Capacitación no encontrada");
            }

            capacitacionRepository.deleteById(id);
        }
        
        @Override
        public List<Capacitacion> sinCurso() {
            return capacitacionRepository.findByCursoIsNull();
        }
        
        @Override
        public List<Capacitacion>
        disponiblesPorTipo(
                TipoTrabajador tipo){

            return capacitacionRepository
                    .findByCursoIsNullAndActivoTrueAndTipoTrabajador(
                            tipo
                    );
        }
	

}