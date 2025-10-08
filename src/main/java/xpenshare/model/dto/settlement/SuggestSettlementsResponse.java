package xpenshare.model.dto.settlement;

import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Serdeable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestSettlementsResponse {
    private Long groupId;
    private List<Suggestion> suggestions;
    private int totalTransfers;
    private String strategy;

    @Serdeable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Suggestion {
        private Long fromUserId;
        private Long toUserId;
        private BigDecimal amount;
    }
}
