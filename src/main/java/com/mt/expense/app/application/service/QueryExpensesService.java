package com.mt.expense.app.application.service;

import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.port.in.QueryExpensesUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.application.query.ExpenseQuery;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.persistence.specification.ExpenseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@org.springframework.stereotype.Service
public final class QueryExpensesService implements QueryExpensesUseCase {

    private static final Logger log = LoggerFactory.getLogger(QueryExpensesService.class);
    private final ExpenseRepositoryPort expenseRepository;
    private final GroupRepositoryPort groupRepository;

    public QueryExpensesService(
            ExpenseRepositoryPort expenseRepository, GroupRepositoryPort groupRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public Page<Expense> queryExpenses(ExpenseQuery query, Pageable pageable) {
        log.debug("Querying expenses");
        if (query.groupId() != null) {
            GroupId groupId = GroupId.of(query.groupId());
            if (groupRepository.findById(groupId).isEmpty()) {
                throw new GroupNotFoundException(groupId);
            }
        }
        Specification<Expense> spec = buildSpec(query);
        return expenseRepository.findAll(spec, pageable);
    }

    private Specification<Expense> buildSpec(ExpenseQuery query) {
        Specification<Expense> spec = (r, q, cb) -> null;
        if (query.groupId() != null) {
            GroupId groupId = GroupId.of(query.groupId());
            spec = spec.and(ExpenseSpecification.hasGroupId(groupId));
        }
        if (query.payerId() != null) {
            UserId payerId = UserId.of(query.payerId());
            spec = spec.and(ExpenseSpecification.hasPayerId(payerId));
        }
        if (query.startDate() != null && query.endDate() != null) {
            spec =
                    spec.and(
                            ExpenseSpecification.isCreatedBetween(
                                    query.startDate(), query.endDate()));
        }
        if (query.settled() != null) {
            spec = spec.and(ExpenseSpecification.isSettled(query.settled()));
        }
        return spec;
    }
}
