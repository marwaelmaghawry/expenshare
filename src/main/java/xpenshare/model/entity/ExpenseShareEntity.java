package xpenshare.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "expense_shares",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"expense_id", "user_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseShareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_share_expense"))
    private ExpenseEntity expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_share_user"))
    private UserEntity user;


    @Column(name = "share_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal shareAmount;
}
