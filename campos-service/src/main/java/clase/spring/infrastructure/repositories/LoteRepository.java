package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    List<Lote> findByCampoIdAndActivoTrue(Long campoId);
    Optional<Lote> findByIdAndCampoIdAndActivoTrue(Long id, Long campoId);
    boolean existsByCampoIdAndNroLoteAndActivoTrue(Long campoId, Integer nroLote);
    boolean existsByCampoIdAndNroLoteAndIdNotAndActivoTrue(Long campoId, Integer nroLote, Long id);
}
