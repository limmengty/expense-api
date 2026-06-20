package com.mt.expense.app.application.command;

import com.mt.expense.app.application.query.SplitEntry;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.SplitStrategy;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.security.UserPrincipal;
import java.util.List;

/**
 * Command object for recording a new expense. A Java 21 record representing the input data needed
 * to create an expense.
 */
public record RecordExpenseCommand(
        GroupId groupId,
        UserId payerId,
        Money totalAmount,
        String description,
        SplitStrategy splitStrategy,
        List<SplitEntry> splitEntries,
        UserPrincipal requestedBy) {
    public RecordExpenseCommand {
        if (groupId == null) throw new IllegalArgumentException("groupId must not be null");
        if (payerId == null) throw new IllegalArgumentException("payerId must not be null");
        if (totalAmount == null) throw new IllegalArgumentException("totalAmount must not be null");
        if (splitStrategy == null)
            throw new IllegalArgumentException("splitStrategy must not be null");
        if (splitEntries == null || splitEntries.isEmpty()) {
            throw new IllegalArgumentException("splitEntries must not be empty");
        }
        if (requestedBy == null) throw new IllegalArgumentException("requestedBy must not be null");
    }
}
