package com.mt.expense.app.infrastructure.web.controller;

import com.mt.expense.app.application.command.DeleteExpenseCommand;
import com.mt.expense.app.application.command.RecordExpenseCommand;
import com.mt.expense.app.application.command.SettleExpenseCommand;
import com.mt.expense.app.application.port.in.*;
import com.mt.expense.app.application.port.out.UserRepositoryPort;
import com.mt.expense.app.application.query.ExpenseQuery;
import com.mt.expense.app.application.query.SplitEntry;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.vo.*;
import com.mt.expense.app.infrastructure.security.KeycloakSecurityAdapter;
import com.mt.expense.app.infrastructure.security.UserPrincipal;
import com.mt.expense.app.infrastructure.web.dto.request.CreateExpenseRequest;
import com.mt.expense.app.infrastructure.web.dto.request.ExpenseQueryParams;
import com.mt.expense.app.infrastructure.web.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for expense operations. Implements the driving adapter pattern for the expense
 * use cases.
 */
@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expenses", description = "Expense recording, querying, settlement, and deletion")
public class ExpenseController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final RecordExpenseUseCase recordExpenseUseCase;
    private final QueryExpensesUseCase queryExpensesUseCase;
    private final GetExpenseByIdUseCase getExpenseByIdUseCase;
    private final SettleExpenseUseCase settleExpenseUseCase;
    private final DeleteExpenseUseCase deleteExpenseUseCase;
    private final UserRepositoryPort userRepository;
    private final KeycloakSecurityAdapter securityAdapter;

    public ExpenseController(
            RecordExpenseUseCase recordExpenseUseCase,
            QueryExpensesUseCase queryExpensesUseCase,
            GetExpenseByIdUseCase getExpenseByIdUseCase,
            SettleExpenseUseCase settleExpenseUseCase,
            DeleteExpenseUseCase deleteExpenseUseCase,
            UserRepositoryPort userRepository,
            KeycloakSecurityAdapter securityAdapter) {
        this.recordExpenseUseCase = recordExpenseUseCase;
        this.queryExpensesUseCase = queryExpensesUseCase;
        this.getExpenseByIdUseCase = getExpenseByIdUseCase;
        this.settleExpenseUseCase = settleExpenseUseCase;
        this.deleteExpenseUseCase = deleteExpenseUseCase;
        this.userRepository = userRepository;
        this.securityAdapter = securityAdapter;
    }

    /** POST /api/v1/expenses - Create a new expense */
    @Operation(
            summary = "Record expense",
            description = "Record a new expense in a group with split strategy")
    @PostMapping
    public ResponseEntity<CreateExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request, @AuthenticationPrincipal Jwt jwt) {

        log.info(
                "Creating expense for group {} by payer {}, splitStrategy={}, splitEntriesCount={}",
                request.groupId(),
                request.payerId(),
                request.splitStrategy(),
                request.splitEntries().size());
        log.info("Split entries: {}", request.splitEntries());

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);

        // Build split strategy
        SplitStrategy strategy = buildSplitStrategy(request);

        // Build split entries
        List<SplitEntry> splitEntries =
                request.splitEntries().stream()
                        .map(e -> new SplitEntry(UserId.of(e.userId()), e.amount(), e.percentage()))
                        .toList();

        RecordExpenseCommand command =
                new RecordExpenseCommand(
                        GroupId.of(request.groupId()),
                        UserId.of(request.payerId()),
                        Money.of(request.totalAmount().amount(), request.totalAmount().currency()),
                        request.description(),
                        strategy,
                        splitEntries,
                        principal);

        ExpenseId expenseId = recordExpenseUseCase.recordExpense(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateExpenseResponse(expenseId.value(), java.time.Instant.now()));
    }

    /** GET /api/v1/expenses - Query expenses with filters */
    @Operation(
            summary = "Query expenses",
            description = "Query expenses with optional filters and pagination")
    @GetMapping
    public ResponseEntity<PageResponse<ExpenseResponse>> getExpenses(
            @ModelAttribute ExpenseQueryParams params) {

        log.debug("Querying expenses with params: {}", params);

        ExpenseQuery query =
                new ExpenseQuery(
                        params.groupId(),
                        params.payerId(),
                        params.startDate(),
                        params.endDate(),
                        params.settled());

        Pageable pageable = buildPageable(params);
        Page<Expense> page = queryExpensesUseCase.queryExpenses(query, pageable);

        // Batch-load all payer IDs from this page to avoid N queries
        List<UUID> payerIds =
                page.getContent().stream().map(e -> e.payerId().value()).distinct().toList();
        Map<UUID, User> payerMap =
                payerIds.stream()
                        .map(id -> userRepository.findById(UserId.of(id)).orElse(null))
                        .filter(u -> u != null)
                        .collect(Collectors.toMap(u -> u.userId().value(), Function.identity()));

        List<ExpenseResponse> content =
                page.getContent().stream()
                        .map(e -> ExpenseResponse.from(e, payerMap.get(e.payerId().value())))
                        .toList();

        return ResponseEntity.ok(
                PageResponse.of(
                        content,
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast()));
    }

    /** GET /api/v1/expenses/{id} - Get single expense by ID */
    @Operation(summary = "Get expense", description = "Retrieve a single expense by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID id) {
        log.debug("Getting expense by id: {}", id);

        Expense expense = getExpenseByIdUseCase.getExpenseById(ExpenseId.of(id));
        User payer = userRepository.findById(expense.payerId()).orElse(null);
        return ResponseEntity.ok(ExpenseResponse.from(expense, payer));
    }

    /** PATCH /api/v1/expenses/{id}/settle - Mark expense as settled */
    @Operation(summary = "Settle expense", description = "Mark an expense as settled")
    @PatchMapping("/{id}/settle")
    public ResponseEntity<Void> settleExpense(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {

        log.info("Settling expense: {}", id);

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        SettleExpenseCommand command = new SettleExpenseCommand(ExpenseId.of(id), principal);

        settleExpenseUseCase.settleExpense(command);

        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/v1/expenses/{id} - Delete an expense */
    @Operation(summary = "Delete expense", description = "Delete an expense (owner or admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {

        log.info("Deleting expense: {}", id);

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        DeleteExpenseCommand command = new DeleteExpenseCommand(ExpenseId.of(id), principal);

        deleteExpenseUseCase.deleteExpense(command);

        return ResponseEntity.noContent().build();
    }

    // Helper methods

    private SplitStrategy buildSplitStrategy(CreateExpenseRequest request) {
        return switch (request.splitStrategy().toUpperCase()) {
            case "EQUAL" -> new SplitStrategy.EqualSplit(request.splitEntries().size());
            case "PERCENTAGE" ->
                    new SplitStrategy.PercentageSplit(
                            request.splitEntries().stream()
                                    .map(
                                            e ->
                                                    new SplitStrategy.PercentageSplit
                                                            .PercentageEntry(
                                                            UserId.of(e.userId()), e.percentage()))
                                    .toList());
            case "EXACT" ->
                    new SplitStrategy.ExactAmountSplit(
                            request.splitEntries().stream()
                                    .map(
                                            e ->
                                                    new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                            UserId.of(e.userId()), e.amount()))
                                    .toList());
            default ->
                    throw new IllegalArgumentException(
                            "Unknown split strategy: " + request.splitStrategy());
        };
    }

    private Pageable buildPageable(ExpenseQueryParams params) {
        Sort sort = Sort.by("createdAt").descending();
        if (params.sort() != null && !params.sort().isEmpty()) {
            String[] parts = params.sort().split(",");
            String field = parts[0];
            Sort.Direction direction =
                    parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;
            sort = Sort.by(direction, field);
        }
        return PageRequest.of(params.page(), params.size(), sort);
    }
}
