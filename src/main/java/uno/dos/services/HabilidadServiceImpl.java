package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import uno.dos.models.entity.Habilidad;
import uno.dos.models.entity.SubArea;
import uno.dos.repositories.HabilidadRepository;

@Service
@RequiredArgsConstructor
public class HabilidadServiceImpl
        implements HabilidadService{

    private final HabilidadRepository
            habilidadRepository;

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
}