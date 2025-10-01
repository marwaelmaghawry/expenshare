package xpenshare.model.dto.expense;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareDto {


    @NotNull
    private Long userId;

    @PositiveOrZero
    private BigDecimal shareAmount;

    private Integer percent;

}
