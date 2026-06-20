package com.mt.expense.app.infrastructure.web.controller;

import com.mt.expense.app.application.port.in.CalculateGroupBalancesUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.application.port.out.UserRepositoryPort;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.Group;
import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.service.Transfer;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.infrastructure.persistence.specification.ExpenseSpecification;
import com.mt.expense.app.infrastructure.security.KeycloakSecurityAdapter;
import com.mt.expense.app.infrastructure.security.UserPrincipal;
import com.mt.expense.app.infrastructure.web.dto.response.ActivityResponse;
import com.mt.expense.app.infrastructure.web.dto.response.BalanceResponse;
import com.mt.expense.app.infrastructure.web.dto.response.DashboardSummaryResponse;
import com.mt.expense.app.infrastructure.web.dto.response.SpendingPeriodResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Aggregated financial overview across all groups")
public class DashboardController {

    private final GroupRepositoryPort groupRepository;
    private final ExpenseRepositoryPort expenseRepository;
    private final CalculateGroupBalancesUseCase calculateGroupBalancesUseCase;
    private final UserRepositoryPort userRepository;
    private final KeycloakSecurityAdapter securityAdapter;

    public DashboardController(
            GroupRepositoryPort groupRepository,
            ExpenseRepositoryPort expenseRepository,
            CalculateGroupBalancesUseCase calculateGroupBalancesUseCase,
            UserRepositoryPort userRepository,
            KeycloakSecurityAdapter securityAdapter) {
        this.groupRepository = groupRepository;
        this.expenseRepository = expenseRepository;
        this.calculateGroupBalancesUseCase = calculateGroupBalancesUseCase;
        this.userRepository = userRepository;
        this.securityAdapter = securityAdapter;
    }

    @Operation(
            summary = "Financial summary",
            description = "Total amounts owed/owed to the current user across all groups")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal Jwt jwt) {
        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        List<Group> groups = groupRepository.findAllByMemberId(principal.userId());

        BigDecimal totalGetBack = BigDecimal.ZERO;
        BigDecimal totalYouOwe = BigDecimal.ZERO;
        int pendingCount = 0;

        for (Group group : groups) {
            for (Transfer t : calculateGroupBalancesUseCase.calculateBalances(group.id())) {
                if (t.to().equals(principal.userId())) {
                    totalGetBack = totalGetBack.add(t.amount().amount());
                    pendingCount++;
                } else if (t.from().equals(principal.userId())) {
                    totalYouOwe = totalYouOwe.add(t.amount().amount());
                    pendingCount++;
                }
            }
        }

        return ResponseEntity.ok(
                new DashboardSummaryResponse(
                        totalGetBack,
                        totalYouOwe,
                        totalGetBack.subtract(totalYouOwe),
                        pendingCount));
    }

    @Operation(
            summary = "Recent activity",
            description = "Latest 10 expenses across all groups the current user belongs to")
    @GetMapping("/activity")
    public ResponseEntity<List<ActivityResponse>> getActivity(@AuthenticationPrincipal Jwt jwt) {
        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        List<Group> groups = groupRepository.findAllByMemberId(principal.userId());

        if (groups.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Map<UUID, String> groupNames =
                groups.stream().collect(Collectors.toMap(g -> g.id().value(), Group::name));

        List<GroupId> groupIds = groups.stream().map(Group::id).toList();
        Specification<Expense> spec = ExpenseSpecification.hasGroupIdIn(groupIds);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Expense> page = expenseRepository.findAll(spec, pageable);

        List<ActivityResponse> activities =
                page.getContent().stream()
                        .map(
                                e ->
                                        new ActivityResponse(
                                                e.id().value().toString(),
                                                "expense",
                                                e.description(),
                                                e.amount().amount(),
                                                e.createdAt().toString(),
                                                groupNames.getOrDefault(e.groupId().value(), ""),
                                                null,
                                                !e.payerId().equals(principal.userId())))
                        .toList();

        return ResponseEntity.ok(activities);
    }

    @Operation(
            summary = "Global balances",
            description = "Net balances with each person across all groups")
    @GetMapping("/balances")
    public ResponseEntity<List<BalanceResponse>> getBalances(@AuthenticationPrincipal Jwt jwt) {
        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        List<Group> groups = groupRepository.findAllByMemberId(principal.userId());

        // Net per counterparty: positive = they owe me, negative = I owe them
        Map<UUID, BigDecimal> netByUser = new LinkedHashMap<>();
        // First group that created a balance with each counterparty (used for settlement recording)
        Map<UUID, UUID> primaryGroupByUser = new LinkedHashMap<>();

        for (Group group : groups) {
            for (Transfer t : calculateGroupBalancesUseCase.calculateBalances(group.id())) {
                if (t.to().equals(principal.userId())) {
                    netByUser.merge(t.from().value(), t.amount().amount(), BigDecimal::add);
                    primaryGroupByUser.putIfAbsent(t.from().value(), group.id().value());
                } else if (t.from().equals(principal.userId())) {
                    netByUser.merge(t.to().value(), t.amount().amount().negate(), BigDecimal::add);
                    primaryGroupByUser.putIfAbsent(t.to().value(), group.id().value());
                }
            }
        }

        // Batch load user display names
        Map<UUID, String> userNames = new HashMap<>();
        for (UUID keycloakId : netByUser.keySet()) {
            String name =
                    userRepository
                            .findByKeycloakId(keycloakId)
                            .map(User::name)
                            .orElse("User " + keycloakId.toString().substring(0, 8));
            userNames.put(keycloakId, name);
        }

        List<BalanceResponse> balances =
                netByUser.entrySet().stream()
                        .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) != 0)
                        .map(
                                e -> {
                                    BigDecimal net = e.getValue();
                                    boolean theyOweMe = net.compareTo(BigDecimal.ZERO) > 0;
                                    UUID groupId = primaryGroupByUser.get(e.getKey());
                                    return new BalanceResponse(
                                            e.getKey().toString(),
                                            userNames.getOrDefault(e.getKey(), "Unknown User"),
                                            null,
                                            net.abs(),
                                            theyOweMe ? "gets back" : "owes",
                                            groupId != null ? groupId.toString() : null);
                                })
                        .toList();

        return ResponseEntity.ok(balances);
    }

    @Operation(
            summary = "Spending history",
            description =
                    "Monthly expense totals for the current user's groups over the last 6 months")
    @GetMapping("/spending-history")
    public ResponseEntity<List<SpendingPeriodResponse>> getSpendingHistory(
            @AuthenticationPrincipal Jwt jwt) {
        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        List<Group> groups = groupRepository.findAllByMemberId(principal.userId());

        List<SpendingPeriodResponse> empty = buildEmptyMonths();

        if (groups.isEmpty()) {
            return ResponseEntity.ok(empty);
        }

        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1);

        List<GroupId> groupIds = groups.stream().map(Group::id).toList();
        Specification<Expense> spec =
                ExpenseSpecification.hasGroupIdIn(groupIds)
                        .and(ExpenseSpecification.isCreatedBetween(sixMonthsAgo, today));

        List<Expense> expenses = expenseRepository.findAllBySpec(spec);

        Map<YearMonth, BigDecimal> sums = new TreeMap<>();
        Map<YearMonth, Integer> counts = new TreeMap<>();
        for (Expense e : expenses) {
            YearMonth ym =
                    YearMonth.from(e.createdAt().atZone(ZoneId.systemDefault()).toLocalDate());
            sums.merge(ym, e.amount().amount(), BigDecimal::add);
            counts.merge(ym, 1, Integer::sum);
        }

        List<SpendingPeriodResponse> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            result.add(
                    new SpendingPeriodResponse(
                            ym.toString(),
                            sums.getOrDefault(ym, BigDecimal.ZERO),
                            counts.getOrDefault(ym, 0)));
        }

        return ResponseEntity.ok(result);
    }

    private List<SpendingPeriodResponse> buildEmptyMonths() {
        List<SpendingPeriodResponse> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            result.add(
                    new SpendingPeriodResponse(
                            YearMonth.now().minusMonths(i).toString(), BigDecimal.ZERO, 0));
        }
        return result;
    }
}
