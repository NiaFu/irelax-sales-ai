package com.irelax.salesai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irelax.salesai.api.dto.MessagingApi;
import com.irelax.salesai.domain.AiSuggestion;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AiSuggestionMapper {
    private final ObjectMapper objectMapper;

    public AiSuggestionMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public MessagingApi.AiSuggestionResponse map(AiSuggestion s) {
        List<String> products = Collections.emptyList();
        if (s.getDetectedProducts() != null && !s.getDetectedProducts().isBlank()) {
            try { products = objectMapper.readValue(s.getDetectedProducts(), new TypeReference<List<String>>() {}); }
            catch (Exception ignored) { products = List.of(); }
        }
        return new MessagingApi.AiSuggestionResponse(
                s.getId(), s.getMessage().getId(), s.getDraftReply(), s.getIntent(), s.getSentiment(), products,
                s.isFollowUpRequired(), s.getSuggestedFollowUpAt(), s.getStageSuggestion(), s.getSummary(), s.getModel(), s.getStatus(), s.getCreatedAt());
    }
}
