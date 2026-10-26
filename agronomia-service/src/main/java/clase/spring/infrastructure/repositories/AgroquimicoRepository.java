package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Agroquimico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgroquimicoRepository extends JpaRepository<Agroquimico, Long> {
    boolean existsByNombreComercialIgnoreCase(String nombreComercial);
    Optional<Agroquimico> findByNombreComercialIgnoreCase(String nombreComercial);
}
