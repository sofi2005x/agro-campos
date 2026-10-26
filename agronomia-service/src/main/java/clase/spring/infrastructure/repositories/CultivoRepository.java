package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Cultivo> findByNombreIgnoreCase(String nombre);
}
