package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import uno.dos.models.entity.Area;
import uno.dos.models.entity.SubArea;
import uno.dos.repositories.SubAreaRepository;

@Service
@RequiredArgsConstructor
public class SubAreaServiceImpl
        implements SubAreaService{

    private final SubAreaRepository
            subAreaRepository;

    @Override
    public List<SubArea> listar(){

        return subAreaRepository
                .findByActivoTrue();
    }

    @Override
    public List<SubArea> listarPorArea(
            Area area){

        return subAreaRepository
                .findByAreaAndActivoTrue(
                        area
                );
    }

    @Override
    public Optional<SubArea> buscarPorId(
            Long id){

        return subAreaRepository
                .findById(id);
    }

    @Override
    public SubArea guardar(
            SubArea subArea){

        return subAreaRepository
                .save(subArea);
    }

    @Override
    public void eliminar(
            Long id){

        SubArea subArea =
                subAreaRepository
                .findById(id)
                .orElseThrow();

        subArea.setActivo(false);

        subAreaRepository.save(
                subArea
        );
    }

    @Override
    public boolean existeNombre(

            String nombreSubArea,

            Area area){

        return subAreaRepository

                .existsByNombreSubAreaAndArea(

                        nombreSubArea,

                        area
                );
    }
}