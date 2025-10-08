package xpenshare.model.dto.settlement;

import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.math.BigDecimal;

@Serdeable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestSettlementsRequest {
    @Builder.Default
    private String strategy = "GREEDY_MIN_TRANSFERS"; // default
    private BigDecimal roundTo;
}
