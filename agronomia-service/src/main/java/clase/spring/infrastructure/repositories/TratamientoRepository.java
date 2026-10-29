package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Long> {
    List<Tratamiento> findByActivoTrue();
    Optional<Tratamiento> findByIdAndActivoTrue(Long id);
    List<Tratamiento> findByPlagaIdAndActivoTrue(Long plagaId);
    List<Tratamiento> findByCultivoIdAndActivoTrue(Long cultivoId);
}
