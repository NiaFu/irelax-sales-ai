package com.irelax.salesai.api.dto;

import com.irelax.salesai.domain.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MessagingApi {
    private MessagingApi() {}

    public record ConversationSummary(
            UUID id,
            UUID customerId,
            String customerName,
            String phone,
            SalesStage salesStage,
            ContactChannel channel,
            String lastMessage,
            MessageDirection lastDirection,
            Instant lastMessageAt
    ) {}

    public record MessageResponse(
            UUID id,
            MessageDirection direction,
            String sender,
            String receiver,
            String content,
            MessageStatus status,
            String externalMessageId,
            String errorCode,
            Instant sentAt,
            Instant receivedAt,
            Instant createdAt
    ) {}

    public record AiSuggestionResponse(
            UUID id,
            UUID messageId,
            String draftReply,
            String intent,
            String sentiment,
            List<String> detectedProducts,
            boolean followUpRequired,
            Instant suggestedFollowUpAt,
            SalesStage stageSuggestion,
            String summary,
            String model,
            AiSuggestionStatus status,
            Instant createdAt
    ) {}

    public record ConversationDetail(
            ConversationSummary conversation,
            List<MessageResponse> messages,
            AiSuggestionResponse latestSuggestion
    ) {}

    public record SendRequest(@NotBlank String content, UUID suggestionId) {}
}
