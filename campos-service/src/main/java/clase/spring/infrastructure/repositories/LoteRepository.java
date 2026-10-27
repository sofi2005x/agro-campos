package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    List<Lote> findByCampoId(Long campoId);
    boolean existsByCampoIdAndNroLote(Long campoId, Integer nroLote);
}
