package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Plaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlagaRepository extends JpaRepository<Plaga, Long> {
    List<Plaga> findByActivoTrue();
    Optional<Plaga> findByIdAndActivoTrue(Long id);
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNotAndActivoTrue(String nombre, Long id);
    Optional<Plaga> findByNombreIgnoreCase(String nombre);
}
