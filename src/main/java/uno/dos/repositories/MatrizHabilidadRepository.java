package uno.dos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uno.dos.models.entity.MatrizHabilidad;

public interface MatrizHabilidadRepository
        extends JpaRepository<MatrizHabilidad, Long> {

    List<MatrizHabilidad> findByActivoTrue();

    @Query("""
        SELECT DISTINCT m
        FROM MatrizHabilidad m
        LEFT JOIN FETCH m.area
        LEFT JOIN FETCH m.subArea
        LEFT JOIN FETCH m.detalles d
        LEFT JOIN FETCH d.habilidad
        WHERE m.activo = true
        """)
    List<MatrizHabilidad> findAllActivasConDetalles();

    @Query("""
        SELECT m
        FROM MatrizHabilidad m
        LEFT JOIN FETCH m.area
        LEFT JOIN FETCH m.subArea
        LEFT JOIN FETCH m.detalles d
        LEFT JOIN FETCH d.habilidad
        LEFT JOIN FETCH m.cursos c
        LEFT JOIN FETCH c.curso
        LEFT JOIN FETCH m.trabajadores t
        LEFT JOIN FETCH t.trabajador
        WHERE m.id = :id
        """)
    Optional<MatrizHabilidad> buscarCompletaPorId(Long id);
}