package com.irelax.salesai.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.UUID;

public final class ProductApi {
    private ProductApi() {}

    public record UpsertRequest(
            @NotBlank String name,
            String brand,
            String model,
            BigDecimal price,
            Integer warrantyYears,
            String description,
            String features,
            String strengths,
            String productUrl,
            String manualUrl,
            Boolean active
    ) {}

    public record Response(
            UUID id,
            String name,
            String brand,
            String model,
            BigDecimal price,
            Integer warrantyYears,
            String description,
            String features,
            String strengths,
            String productUrl,
            String manualUrl,
            boolean active
    ) {}
}
