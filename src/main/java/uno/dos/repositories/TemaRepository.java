package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.Tema;

public interface TemaRepository extends JpaRepository<Tema, Long> {

    List<Tema> findByCurso_IdOrderByOrdenAsc(Long cursoId);
}