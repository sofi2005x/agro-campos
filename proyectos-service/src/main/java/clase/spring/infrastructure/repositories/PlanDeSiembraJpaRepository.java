package clase.spring.infrastructure.repositories;

import clase.spring.domain.models.EstadoPlan;
import clase.spring.domain.models.PlanDeSiembra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanDeSiembraJpaRepository extends JpaRepository<PlanDeSiembra, Long> {

    List<PlanDeSiembra> findByEstado(EstadoPlan estado);

    // Histórico de un lote, del más reciente al más antiguo
    List<PlanDeSiembra> findByLoteIdOrderByFechaCreacionDesc(Long loteId);
}
