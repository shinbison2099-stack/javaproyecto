package uno.dos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uno.dos.models.entity.PlanCarrera;

public interface PlanCarreraRepository extends JpaRepository<PlanCarrera, Long> {
}