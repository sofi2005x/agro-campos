package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Agroquimico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgroquimicoRepository extends JpaRepository<Agroquimico, Long> {
    List<Agroquimico> findByActivoTrue();
    Optional<Agroquimico> findByIdAndActivoTrue(Long id);
    boolean existsByNombreComercialIgnoreCaseAndActivoTrue(String nombreComercial);
    Optional<Agroquimico> findByNombreComercialIgnoreCase(String nombreComercial);
    Optional<Agroquimico> findByNombreComercialIgnoreCaseAndActivoTrue(String nombreComercial);
}
