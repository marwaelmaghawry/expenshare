package xpenshare.repository.facade;

import jakarta.inject.Singleton;
import xpenshare.exception.NotFoundException;
import xpenshare.model.entity.SettlementEntity;
import xpenshare.repository.SettlementRepository;

import java.util.List;

@Singleton
public class SettlementRepositoryFacade {

    private final SettlementRepository repository;

    public SettlementRepositoryFacade(SettlementRepository repository) {
        this.repository = repository;
    }

    public SettlementEntity save(SettlementEntity entity) {
        return repository.save(entity);
    }

    public SettlementEntity findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Settlement not found"));
    }

    public List<SettlementEntity> findByGroupId(Long groupId) {
        return repository.findByGroupGroupId(groupId);
    }
}
