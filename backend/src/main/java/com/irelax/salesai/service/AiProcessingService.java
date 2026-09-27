package com.irelax.salesai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irelax.salesai.ai.SalesAiAssistant;
import com.irelax.salesai.ai.SalesAiResult;
import com.irelax.salesai.api.dto.MessagingApi;
import com.irelax.salesai.common.ResourceNotFoundException;
import com.irelax.salesai.domain.*;
import com.irelax.salesai.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AiProcessingService {
    private static final Logger log = LoggerFactory.getLogger(AiProcessingService.class);
    private final MessageRepository messages;
    private final ProductRepository products;
    private final KnowledgeEntryRepository knowledge;
    private final SalesAssistantProfileRepository profiles;
    private final AiSuggestionRepository suggestions;
    private final CustomerProductInterestRepository interests;
    private final SalesAiAssistant aiAssistant;
    private final ObjectMapper objectMapper;
    private final FollowUpService followUpService;
    private final AiSuggestionMapper mapper;

    public AiProcessingService(MessageRepository messages, ProductRepository products, KnowledgeEntryRepository knowledge,
                               SalesAssistantProfileRepository profiles, AiSuggestionRepository suggestions,
                               CustomerProductInterestRepository interests, SalesAiAssistant aiAssistant,
                               ObjectMapper objectMapper, FollowUpService followUpService, AiSuggestionMapper mapper) {
        this.messages = messages;
        this.products = products;
        this.knowledge = knowledge;
        this.profiles = profiles;
        this.suggestions = suggestions;
        this.interests = interests;
        this.aiAssistant = aiAssistant;
        this.objectMapper = objectMapper;
        this.followUpService = followUpService;
        this.mapper = mapper;
    }

    @Async
    public void processInboundAsync(UUID messageId) {
        try {
            Message message = messages.findById(messageId).orElse(null);
            if (message == null) return;
            if (suggestions.findFirstByOwnerIdAndMessageIdOrderByCreatedAtDesc(message.getOwnerId(), messageId).isPresent()) return;
            generateForMessage(messageId, null);
        } catch (Exception e) {
            log.error("AI processing failed for message {}", messageId, e);
        }
    }

    @Transactional
    public MessagingApi.AiSuggestionResponse generateForMessage(UUID messageId, String expectedOwnerId) {
        Message inbound = expectedOwnerId == null
                ? messages.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found"))
                : messages.findByIdAndOwnerId(messageId, expectedOwnerId).orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        if (inbound.getDirection() != MessageDirection.INBOUND) {
            throw new IllegalArgumentException("AI suggestions can only be generated from inbound messages");
        }
        String ownerId = inbound.getOwnerId();
        Customer customer = inbound.getConversation().getCustomer();
        List<Message> recent = new ArrayList<>(messages.findByConversationIdAndOwnerIdOrderByCreatedAtDesc(
                inbound.getConversation().getId(), ownerId, PageRequest.of(0, 20)));
        Collections.reverse(recent);
        List<Product> productList = products.findByOwnerIdAndActiveTrueOrderByNameAsc(ownerId);
        SalesAssistantProfile profile = profiles.findByOwnerId(ownerId).orElseGet(() -> defaultProfile(ownerId));
        List<KnowledgeEntry> entries = knowledge.findByOwnerIdAndActiveTrueOrderByUpdatedAtDesc(ownerId);

        SalesAiResult result = aiAssistant.analyseAndDraft(customer, inbound, recent, productList, profile, entries);
        AiSuggestion suggestion = new AiSuggestion();
        suggestion.setOwnerId(ownerId);
        suggestion.setMessage(inbound);
        suggestion.setDraftReply(result.draftReply());
        suggestion.setIntent(result.intent());
        suggestion.setSentiment(result.sentiment());
        suggestion.setDetectedProducts(writeJson(result.detectedProducts()));
        suggestion.setFollowUpRequired(result.followUpRequired());
        if (result.followUpRequired()) {
            int days = result.followUpDays() == null ? 3 : Math.max(0, result.followUpDays());
            suggestion.setSuggestedFollowUpAt(Instant.now().plus(days, ChronoUnit.DAYS));
        }
        suggestion.setStageSuggestion(result.stageSuggestion());
        suggestion.setSummary(result.summary());
        suggestion.setModel(result.model());
        suggestion.setStatus(AiSuggestionStatus.PENDING);
        suggestion = suggestions.save(suggestion);

        persistAiProductPredictions(ownerId, customer, productList, result.detectedProducts(), result.summary());
        if (suggestion.isFollowUpRequired()) {
            followUpService.createAiSuggested(ownerId, customer, suggestion.getSuggestedFollowUpAt(), result.summary());
        }
        return mapper.map(suggestion);
    }

    private void persistAiProductPredictions(String ownerId, Customer customer, List<Product> products,
                                             List<String> detectedNames, String reason) {
        if (detectedNames == null) return;
        for (String name : detectedNames) {
            products.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().ifPresent(product -> {
                CustomerProductInterest interest = interests.findByOwnerIdAndCustomerIdAndProductIdAndSource(
                                ownerId, customer.getId(), product.getId(), InterestSource.AI)
                        .orElseGet(CustomerProductInterest::new);
                interest.setOwnerId(ownerId);
                interest.setCustomer(customer);
                interest.setProduct(product);
                interest.setSource(InterestSource.AI);
                interest.setInterestLevel(InterestLevel.MEDIUM);
                interest.setConfidence(new BigDecimal("0.700"));
                interest.setReason(reason);
                interests.save(interest);
            });
        }
    }

    private SalesAssistantProfile defaultProfile(String ownerId) {
        SalesAssistantProfile profile = new SalesAssistantProfile();
        profile.setOwnerId(ownerId);
        profile.setDisplayName("Nia");
        profile.setTone("polite, natural, concise, low-pressure, not corporate");
        profile.setRules("Do not invent prices, discounts, warranties, stock or delivery promises. Keep SMS replies short and human.");
        return profiles.save(profile);
    }

    private String writeJson(List<String> values) {
        try { return objectMapper.writeValueAsString(values == null ? List.of() : values); }
        catch (Exception e) { return "[]"; }
    }
}
