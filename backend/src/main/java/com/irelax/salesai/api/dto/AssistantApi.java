package com.irelax.salesai.api.dto;

import jakarta.validation.constraints.NotBlank;

public final class AssistantApi {
    private AssistantApi() {}
    public record QueryRequest(@NotBlank String query) {}
    public record QueryResponse(String answer) {}
}
