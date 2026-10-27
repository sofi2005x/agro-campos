package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Campo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampoRepository extends JpaRepository<Campo, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Campo> findByNombre(String nombre);
}
