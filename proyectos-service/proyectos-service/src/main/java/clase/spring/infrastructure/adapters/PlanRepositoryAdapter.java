package clase.spring.infrastructure.adapters;

import clase.spring.domain.models.EstadoPlan;
import clase.spring.domain.models.PlanDeSiembra;
import clase.spring.domain.ports.PlanRepository;
import clase.spring.infrastructure.repositories.PlanDeSiembraJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adapter de {@link PlanRepository}: implementa el port con Spring Data JPA. */
@AllArgsConstructor
@Component
public class PlanRepositoryAdapter implements PlanRepository {

    private final PlanDeSiembraJpaRepository jpaRepository;

    @Override
    public PlanDeSiembra guardar(PlanDeSiembra plan) {
        return jpaRepository.save(plan);
    }

    @Override
    public Optional<PlanDeSiembra> buscarPorId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<PlanDeSiembra> buscarTodos() {
        return jpaRepository.findAll();
    }

    @Override
    public List<PlanDeSiembra> buscarPorEstado(EstadoPlan estado) {
        return jpaRepository.findByEstado(estado);
    }

    @Override
    public List<PlanDeSiembra> buscarPorLote(Long loteId) {
        return jpaRepository.findByLoteIdOrderByFechaCreacionDesc(loteId);
    }
}
