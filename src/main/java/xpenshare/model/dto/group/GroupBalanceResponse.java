package xpenshare.model.dto.group;

import io.micronaut.serde.annotation.Serdeable;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Serdeable
public class GroupBalanceResponse {
    private Long groupId;
    private List<UserBalance> balances;
    private Instant calculatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Serdeable
    public static class UserBalance {
        private Long userId;
        private BigDecimal balance;
    }
}
