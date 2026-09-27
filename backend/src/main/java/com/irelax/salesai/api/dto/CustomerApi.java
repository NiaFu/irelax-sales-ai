package com.irelax.salesai.api.dto;

import com.irelax.salesai.domain.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class CustomerApi {
    private CustomerApi() {}

    public record UpsertRequest(
            @NotBlank String firstName,
            String lastName,
            @NotBlank String phone,
            String email,
            ContactChannel preferredChannel,
            SalesStage salesStage,
            LocalDate firstVisitDate,
            String firstVisitLocation,
            String feedback,
            BigDecimal budgetMin,
            BigDecimal budgetMax,
            String notes,
            Instant nextFollowUpAt
    ) {}

    public record InterestResponse(
            UUID id,
            UUID productId,
            String productName,
            InterestLevel level,
            InterestSource source,
            BigDecimal confidence,
            String reason
    ) {}

    public record NoteResponse(UUID id, String body, Instant createdAt) {}

    public record Response(
            UUID id,
            String firstName,
            String lastName,
            String displayName,
            String phone,
            String email,
            ContactChannel preferredChannel,
            SalesStage salesStage,
            LocalDate firstVisitDate,
            String firstVisitLocation,
            String feedback,
            BigDecimal budgetMin,
            BigDecimal budgetMax,
            String notes,
            Instant lastContactAt,
            Instant nextFollowUpAt,
            Instant createdAt,
            Instant updatedAt,
            List<InterestResponse> interests,
            List<NoteResponse> customerNotes
    ) {}

    public record InterestRequest(UUID productId, InterestLevel level, String reason) {}
    public record NoteRequest(@NotBlank String body) {}
}
