package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import uno.dos.models.entity.Habilidad;
import uno.dos.models.entity.SubArea;
import uno.dos.repositories.HabilidadRepository;
import uno.dos.repositories.SubAreaRepository;

@Service
@RequiredArgsConstructor
public class HabilidadServiceImpl
        implements HabilidadService{

    private final HabilidadRepository
            habilidadRepository;
    
    private final SubAreaRepository subAreaRepository;

    @Override
    public List<Habilidad> listar(){

        return habilidadRepository
                .findByActivoTrue();
    }

    @Override
    public List<Habilidad> listarPorSubArea(
            SubArea subArea){

        return habilidadRepository
                .findBySubAreaAndActivoTrue(
                        subArea
                );
    }

    @Override
    public Optional<Habilidad> buscarPorId(
            Long id){

        return habilidadRepository
                .findById(id);
    }

    @Override
    public Habilidad guardar(
            Habilidad habilidad){

        return habilidadRepository
                .save(habilidad);
    }

    @Override
    public void eliminar(
            Long id){

        Habilidad habilidad =
                habilidadRepository
                .findById(id)
                .orElseThrow();

        habilidad.setActivo(false);

        habilidadRepository.save(
                habilidad
        );
    }

    @Override
    public boolean existeNombre(
            String nombreHabilidad,

            SubArea subArea){

        return habilidadRepository
                .existsByNombreHabilidadAndSubArea(
                        nombreHabilidad,
                        subArea
                );
    }
    
    @Override
    public List<Habilidad> buscarPorSubArea(
            Long subAreaId){

        return habilidadRepository
                .findBySubAreaId(subAreaId);
    }
    
    @Override
    public Habilidad guardarDesdeWizard(
            Long subAreaId,
            String nombreHabilidad){

        String nombreLimpio =
                nombreHabilidad == null
                ? ""
                : nombreHabilidad.trim();

        if(nombreLimpio.isBlank()){
            throw new RuntimeException("El nombre de la habilidad es obligatorio");
        }

        SubArea subArea =
                subAreaRepository
                        .findById(subAreaId)
                        .orElseThrow(() ->
                                new RuntimeException("Subárea no encontrada"));

        Habilidad habilidad =
                new Habilidad();

        habilidad.setNombreHabilidad(nombreLimpio);
        habilidad.setDescripcion(nombreLimpio);
        habilidad.setOrden(1);
        habilidad.setSubArea(subArea);
        habilidad.setActivo(true);

        return habilidadRepository.save(habilidad);
    }
}