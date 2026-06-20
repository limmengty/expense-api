package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateGroupRequest(
        @NotBlank(message = "group name is required")
                @Size(max = 255, message = "group name must not exceed 255 characters")
                String name,
        Set<UUID> initialMembers) {}
