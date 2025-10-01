package xpenshare.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import xpenshare.model.dto.expense.*;
import xpenshare.service.ExpenseService;

@Controller("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Post
    public HttpResponse<ExpenseDto> addExpense(@Body @Valid CreateExpenseRequest request) {
        return HttpResponse.created(expenseService.addExpense(request));
    }
}
