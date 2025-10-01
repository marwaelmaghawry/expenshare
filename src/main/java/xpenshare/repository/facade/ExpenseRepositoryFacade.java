package xpenshare.repository.facade;

import jakarta.inject.Singleton;
import xpenshare.exception.NotFoundException;
import xpenshare.model.entity.ExpenseEntity;
import xpenshare.repository.ExpenseRepository;

@Singleton
public class ExpenseRepositoryFacade {

    private final ExpenseRepository expenseRepository;

    public ExpenseRepositoryFacade(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ExpenseEntity save(ExpenseEntity expense) {
        return expenseRepository.save(expense);
    }

    public ExpenseEntity findByIdOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found"));
    }
}
