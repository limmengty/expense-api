package com.mt.expense.app.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** JPA Entity for Expense persistence. */
@Setter
@Getter
@Entity
@Table(name = "expenses")
public class ExpenseJpaEntity {

    // Getters and Setters
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "payer_id", nullable = false)
    private UUID payerId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "split_strategy", nullable = false, length = 50)
    private String splitStrategy;

    @Column(name = "settled", nullable = false)
    private boolean settled;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "expense",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ExpenseSplitJpaEntity> splits = new ArrayList<>();

    public ExpenseJpaEntity() {}

    public void addSplit(ExpenseSplitJpaEntity split) {
        splits.add(split);
        split.setExpense(this);
    }

    public void removeSplit(ExpenseSplitJpaEntity split) {
        splits.remove(split);
        split.setExpense(null);
    }
}
