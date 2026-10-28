package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Campo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampoRepository extends JpaRepository<Campo, Long> {
    List<Campo> findByActivoTrue();
    Optional<Campo> findByIdAndActivoTrue(Long id);
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNotAndActivoTrue(String nombre, Long id);
    Optional<Campo> findByNombre(String nombre);
}
