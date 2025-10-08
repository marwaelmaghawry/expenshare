package xpenshare.model.dto.expense;

import jakarta.validation.constraints.*;
import lombok.Data;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.util.List;

@Serdeable
@Data
public class CreateExpenseRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private Long paidBy;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 1, max = 255)
    private String description;

    @NotNull
    private SplitType splitType;

    private List<Long> participants;
    private List<ShareRequest> shares;

    @Serdeable
    @Data
    public static class ShareRequest {
        private Long userId;
        private BigDecimal amount;
        private Integer percent;
    }

    public enum SplitType {
        EQUAL, EXACT, PERCENT
    }
}
