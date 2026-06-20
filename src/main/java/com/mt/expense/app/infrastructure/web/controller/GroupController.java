package com.mt.expense.app.infrastructure.web.controller;

import com.mt.expense.app.application.command.RemoveGroupMemberCommand;
import com.mt.expense.app.application.command.RenameGroupCommand;
import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.exception.NotGroupMemberException;
import com.mt.expense.app.application.port.in.CalculateGroupBalancesUseCase;
import com.mt.expense.app.application.port.in.QueryExpensesUseCase;
import com.mt.expense.app.application.port.in.RemoveGroupMemberUseCase;
import com.mt.expense.app.application.port.in.RenameGroupUseCase;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.application.port.out.UserRepositoryPort;
import com.mt.expense.app.application.query.ExpenseQuery;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.Group;
import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.service.Transfer;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.security.KeycloakSecurityAdapter;
import com.mt.expense.app.infrastructure.security.UserPrincipal;
import com.mt.expense.app.infrastructure.web.dto.request.AddMemberRequest;
import com.mt.expense.app.infrastructure.web.dto.request.CreateGroupRequest;
import com.mt.expense.app.infrastructure.web.dto.request.ExpenseQueryParams;
import com.mt.expense.app.infrastructure.web.dto.request.RenameGroupRequest;
import com.mt.expense.app.infrastructure.web.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/** REST Controller for group operations. */
@RestController
@RequestMapping("/api/v1/groups")
@Tag(name = "Groups", description = "Group management and balance calculation")
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    private final CalculateGroupBalancesUseCase calculateGroupBalancesUseCase;
    private final QueryExpensesUseCase queryExpensesUseCase;
    private final RemoveGroupMemberUseCase removeGroupMemberUseCase;
    private final RenameGroupUseCase renameGroupUseCase;
    private final GroupRepositoryPort groupRepository;
    private final UserRepositoryPort userRepository;
    private final KeycloakSecurityAdapter securityAdapter;

    public GroupController(
            CalculateGroupBalancesUseCase calculateGroupBalancesUseCase,
            QueryExpensesUseCase queryExpensesUseCase,
            RemoveGroupMemberUseCase removeGroupMemberUseCase,
            RenameGroupUseCase renameGroupUseCase,
            GroupRepositoryPort groupRepository,
            UserRepositoryPort userRepository,
            KeycloakSecurityAdapter securityAdapter) {
        this.calculateGroupBalancesUseCase = calculateGroupBalancesUseCase;
        this.queryExpensesUseCase = queryExpensesUseCase;
        this.removeGroupMemberUseCase = removeGroupMemberUseCase;
        this.renameGroupUseCase = renameGroupUseCase;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.securityAdapter = securityAdapter;
    }

    /** GET /api/v1/groups - List all groups the authenticated user belongs to */
    @Operation(
            summary = "List my groups",
            description = "Returns all groups the current user is a member of")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthenticationPrincipal Jwt jwt) {
        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        List<GroupResponse> response =
                groupRepository.findAllByMemberId(principal.userId()).stream()
                        .map(GroupResponse::from)
                        .toList();
        return ResponseEntity.ok(response);
    }

    /** GET /api/v1/groups/{groupId} - Get a single group (only if the user is a member) */
    @Operation(
            summary = "Get group",
            description = "Returns a single group by ID — caller must be a member")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt) {

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        GroupId gid = GroupId.of(groupId);

        Group group =
                groupRepository.findById(gid).orElseThrow(() -> new GroupNotFoundException(gid));

        if (!group.isMember(principal.userId())) {
            throw new NotGroupMemberException(gid, principal.userId());
        }

        return ResponseEntity.ok(GroupResponse.from(group));
    }

    /** POST /api/v1/groups - Create a new group */
    @Operation(summary = "Create group", description = "Create a new expense group")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request, @AuthenticationPrincipal Jwt jwt) {

        log.info("Creating group: {}", request.name());

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);

        Set<UserId> memberIds =
                request.initialMembers() != null
                        ? request.initialMembers().stream()
                                .map(UserId::of)
                                .collect(Collectors.toSet())
                        : Set.of();

        Group group = Group.create(request.name(), principal.userId(), memberIds);

        Group saved = groupRepository.save(group);

        return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(saved));
    }

    /** GET /api/v1/groups/{groupId}/balances - Calculate group balances */
    @Operation(
            summary = "Get group balances",
            description = "Calculate and return all member balances for a group")
    @GetMapping("/{groupId}/balances")
    public ResponseEntity<BalanceSummaryResponse> getGroupBalances(@PathVariable UUID groupId) {

        log.info("Calculating balances for group: {}", groupId);

        GroupId id = GroupId.of(groupId);
        List<Transfer> transfers = calculateGroupBalancesUseCase.calculateBalances(id);

        return ResponseEntity.ok(BalanceSummaryResponse.of(id, transfers));
    }

    /** GET /api/v1/groups/{groupId}/expenses - Get group expenses */
    @Operation(
            summary = "Get group expenses",
            description = "Retrieve paginated expenses for a specific group")
    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<PageResponse<ExpenseResponse>> getGroupExpenses(
            @PathVariable UUID groupId, @ModelAttribute ExpenseQueryParams params) {

        log.debug("Getting expenses for group: {}", groupId);

        // Build query with forced groupId filter
        ExpenseQuery query =
                new ExpenseQuery(
                        groupId,
                        params.payerId(),
                        params.startDate(),
                        params.endDate(),
                        params.settled());

        Pageable pageable = buildPageable(params);
        Page<Expense> page = queryExpensesUseCase.queryExpenses(query, pageable);

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

    /** GET /api/v1/groups/{groupId}/members - List group members with display names */
    @Operation(
            summary = "Get group members",
            description = "Returns all members with resolved display names")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt) {

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        GroupId gid = GroupId.of(groupId);

        Group group =
                groupRepository.findById(gid).orElseThrow(() -> new GroupNotFoundException(gid));

        if (!group.isMember(principal.userId())) {
            throw new NotGroupMemberException(gid, principal.userId());
        }

        List<GroupMemberResponse> members =
                group.memberIds().stream()
                        .map(
                                memberId -> {
                                    String name =
                                            userRepository
                                                    .findByKeycloakId(memberId.value())
                                                    .map(User::name)
                                                    .orElse(
                                                            "User "
                                                                    + memberId.value()
                                                                            .toString()
                                                                            .substring(0, 8));
                                    String email =
                                            userRepository
                                                    .findByKeycloakId(memberId.value())
                                                    .map(User::email)
                                                    .orElse(null);
                                    return new GroupMemberResponse(
                                            memberId.value().toString(), name, email);
                                })
                        .toList();

        return ResponseEntity.ok(members);
    }

    /** POST /api/v1/groups/{groupId}/members - Add member to group */
    @Operation(summary = "Add member", description = "Add a user to an existing group")
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable UUID groupId, @Valid @RequestBody AddMemberRequest request) {

        log.info("Adding member {} to group {}", request.userId(), groupId);

        GroupId gid = GroupId.of(groupId);
        UserId userId = UserId.of(request.userId());

        Group updated = groupRepository.addMember(gid, userId);

        return ResponseEntity.ok(GroupResponse.from(updated));
    }

    /** DELETE /api/v1/groups/{groupId}/members/{userId} - Remove member from group */
    @Operation(summary = "Remove member", description = "Remove a user from an existing group")
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("Removing member {} from group {}", userId, groupId);

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        RemoveGroupMemberCommand command =
                new RemoveGroupMemberCommand(GroupId.of(groupId), UserId.of(userId), principal);

        removeGroupMemberUseCase.removeMember(command);

        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/v1/groups/{groupId} - Rename group */
    @Operation(summary = "Rename group", description = "Rename an existing group")
    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupResponse> renameGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody RenameGroupRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("Renaming group {} to '{}'", groupId, request.name());

        UserPrincipal principal = securityAdapter.extractPrincipalFromJwt(jwt);
        RenameGroupCommand command =
                new RenameGroupCommand(GroupId.of(groupId), request.name(), principal);

        renameGroupUseCase.renameGroup(command);

        Group updated =
                groupRepository
                        .findById(GroupId.of(groupId))
                        .orElseThrow(() -> new GroupNotFoundException(GroupId.of(groupId)));

        return ResponseEntity.ok(GroupResponse.from(updated));
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
        int page = params.page() != null ? params.page() : 0;
        int size = params.size() != null ? params.size() : 20;
        return PageRequest.of(page, size, sort);
    }
}
