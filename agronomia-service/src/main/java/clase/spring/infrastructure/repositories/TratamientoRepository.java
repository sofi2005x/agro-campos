package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Long> {
    List<Tratamiento> findByPlagaId(Long plagaId);
    List<Tratamiento> findByCultivoId(Long cultivoId);
}
