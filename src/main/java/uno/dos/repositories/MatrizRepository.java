package uno.dos.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.Matriz;

public interface MatrizRepository extends JpaRepository<Matriz, Long> {
	
	Optional<Matriz> findByNombre(String nombre);
}