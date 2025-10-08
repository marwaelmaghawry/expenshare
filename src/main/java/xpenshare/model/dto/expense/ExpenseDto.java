package xpenshare.model.dto.expense;

import lombok.Builder;
import lombok.Data;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Serdeable

@Data
@Builder
public class ExpenseDto {
    private Long expenseId;
    private Long groupId;
    private Long paidById;
    private BigDecimal amount;
    private String description;
    private List<ShareDto> split;
    private Instant createdAt;
}
