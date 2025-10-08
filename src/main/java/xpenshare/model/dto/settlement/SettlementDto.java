package xpenshare.model.dto.settlement;
import io.micronaut.serde.annotation.Serdeable;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Serdeable

@Data
@Builder
public class SettlementDto {
    private Long settlementId;
    private Long groupId;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String method;
    private String note;
    private String reference;
    private String status;
    private Instant createdAt;
    private Instant confirmedAt;
}
