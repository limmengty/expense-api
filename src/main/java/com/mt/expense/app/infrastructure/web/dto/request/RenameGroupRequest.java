package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameGroupRequest(
        @NotBlank(message = "name is required")
                @Size(max = 255, message = "name must not exceed 255 characters")
                String name) {}
