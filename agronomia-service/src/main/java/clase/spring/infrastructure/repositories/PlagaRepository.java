package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Plaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlagaRepository extends JpaRepository<Plaga, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Plaga> findByNombreIgnoreCase(String nombre);
}
