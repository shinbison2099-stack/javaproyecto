package uno.dos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.PuestoCurso;

public interface PuestoCursoRepository extends JpaRepository<PuestoCurso, Long>{

boolean existsByPuestoIdAndCursoId(
    Long puestoId,
    Long cursoId
);

void deleteByPuestoId(Long puestoId);
}