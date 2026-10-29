package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {
    List<Cultivo> findByActivoTrue();
    Optional<Cultivo> findByIdAndActivoTrue(Long id);
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNotAndActivoTrue(String nombre, Long id);
    Optional<Cultivo> findByNombreIgnoreCase(String nombre);
}
