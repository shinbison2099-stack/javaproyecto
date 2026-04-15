package uno.dos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uno.dos.models.entity.CategoriaPuesto;

public interface CategoriaPuestoRepository
extends JpaRepository<CategoriaPuesto, Long> {

List<CategoriaPuesto> findByActivoTrue();
}