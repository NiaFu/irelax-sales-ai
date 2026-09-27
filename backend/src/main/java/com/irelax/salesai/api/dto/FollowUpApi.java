package com.irelax.salesai.api.dto;

import com.irelax.salesai.domain.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class FollowUpApi {
    private FollowUpApi() {}

    public record CreateRequest(
            @NotNull UUID customerId,
            FollowUpType type,
            @NotNull Instant dueAt,
            String reason,
            TaskPriority priority
    ) {}

    public record Response(
            UUID id,
            UUID customerId,
            String customerName,
            FollowUpType type,
            Instant dueAt,
            TaskStatus status,
            String reason,
            TaskPriority priority,
            boolean aiGenerated,
            Instant completedAt
    ) {}
}
