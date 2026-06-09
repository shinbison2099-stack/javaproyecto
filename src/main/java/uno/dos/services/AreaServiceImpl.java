package uno.dos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import uno.dos.models.entity.Area;
import uno.dos.repositories.AreaRepository;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl
        implements AreaService{

    private final AreaRepository areaRepository;

    @Override
    public List<Area> listar(){

        return areaRepository
                .findByActivoTrue();
    }

    @Override
    public List<Area> listarEliminadas(){

        return areaRepository
                .findByActivoFalse();
    }

    @Override
    public Optional<Area> buscarPorId(
            Long id){

        return areaRepository
                .findById(id);
    }

    @Override
    public Area guardar(
            Area area){

        return areaRepository
                .save(area);
    }

    @Override
    public void eliminar(
            Long id){

        Area area = areaRepository
                .findById(id)
                .orElseThrow();

        area.setActivo(false);

        areaRepository.save(area);
    }

    @Override
    public boolean existeNombre(
            String nombreArea){

        return areaRepository
                .existsByNombreArea(
                        nombreArea
                );
    }
}