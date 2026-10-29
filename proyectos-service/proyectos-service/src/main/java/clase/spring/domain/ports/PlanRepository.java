package clase.spring.domain.ports;

import clase.spring.domain.models.EstadoPlan;
import clase.spring.domain.models.PlanDeSiembra;

import java.util.List;
import java.util.Optional;

/** Persistencia de planes de siembra, expresada en términos del dominio. */
public interface PlanRepository {

    PlanDeSiembra guardar(PlanDeSiembra plan);

    Optional<PlanDeSiembra> buscarPorId(Long id);

    List<PlanDeSiembra> buscarTodos();

    List<PlanDeSiembra> buscarPorEstado(EstadoPlan estado);

    /** Histórico del lote, del plan más reciente al más antiguo. */
    List<PlanDeSiembra> buscarPorLote(Long loteId);
}
