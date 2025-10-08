package xpenshare.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable

@Entity
@Table(name = "settlements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_settlement_group"))
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_settlement_from_user"))
    private UserEntity fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_settlement_to_user"))
    private UserEntity toUser;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Method method = Method.OTHER;

    public enum Method {
        CASH, BANK_TRANSFER, CREDIT_CARD, OTHER
    }

    @Column(length = 255)
    private String note;

    @Column(length = 64)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, CONFIRMED, CANCELED
    }

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant confirmedAt;
}
