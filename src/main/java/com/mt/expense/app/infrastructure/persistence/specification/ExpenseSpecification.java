package com.mt.expense.app.infrastructure.persistence.specification;

import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.vo.ExpenseId;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecification {

    private ExpenseSpecification() {}

    public static Specification<Expense> hasGroupId(GroupId groupId) {
        return (root, query, cb) -> cb.equal(root.get("groupId"), groupId.value());
    }

    public static Specification<Expense> hasPayerId(UserId payerId) {
        return (root, query, cb) -> cb.equal(root.get("payerId"), payerId.value());
    }

    public static Specification<Expense> hasExpenseId(ExpenseId expenseId) {
        return (root, query, cb) -> cb.equal(root.get("id"), expenseId.value());
    }

    public static Specification<Expense> isSettled(boolean settled) {
        return (root, query, cb) -> cb.equal(root.get("settled"), settled);
    }

    public static Specification<Expense> hasGroupIdIn(Collection<GroupId> groupIds) {
        if (groupIds.isEmpty()) {
            return (root, query, cb) -> cb.disjunction();
        }
        List<UUID> ids = groupIds.stream().map(GroupId::value).toList();
        return (root, query, cb) -> root.get("groupId").in(ids);
    }

    public static Specification<Expense> isCreatedBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            var start =
                    startDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant();
            var end =
                    endDate.atTime(LocalTime.MAX)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toInstant();
            return cb.between(root.get("createdAt"), start, end);
        };
    }
}
