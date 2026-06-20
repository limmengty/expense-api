package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.model.Group;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Response DTO for group details. */
public record GroupResponse(
        UUID id, String name, Set<UUID> memberIds, UUID createdBy, Instant createdAt) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.id().value(),
                group.name(),
                group.memberIds().stream()
                        .map(u -> u.value())
                        .collect(java.util.stream.Collectors.toSet()),
                group.createdBy().value(),
                group.createdAt());
    }
}
