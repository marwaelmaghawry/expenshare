package xpenshare.model.dto.settlement;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSettlementRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private Long fromUserId;

    @NotNull
    private Long toUserId;

    @NotNull
    @Positive(message = "Settlement amount must be positive")
    private BigDecimal amount;

    private String method;
    private String note;
    private String reference;
    private Boolean enforceOwedLimit = true;
}

