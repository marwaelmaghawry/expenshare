package xpenshare.repository.facade;

import jakarta.inject.Singleton;
import xpenshare.model.entity.ExpenseShareEntity;
import xpenshare.repository.ExpenseShareRepository;

@Singleton
public class ExpenseShareRepositoryFacade {

    private final ExpenseShareRepository expenseShareRepository;

    public ExpenseShareRepositoryFacade(ExpenseShareRepository expenseShareRepository) {
        this.expenseShareRepository = expenseShareRepository;
    }

    public ExpenseShareEntity save(ExpenseShareEntity share) {
        return expenseShareRepository.save(share);
    }
}
