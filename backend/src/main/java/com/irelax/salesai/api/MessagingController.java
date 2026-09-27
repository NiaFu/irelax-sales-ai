package com.irelax.salesai.api;

import com.irelax.salesai.api.dto.MessagingApi;
import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.service.AiProcessingService;
import com.irelax.salesai.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MessagingController {
    private final MessagingService messaging;
    private final AiProcessingService ai;
    private final OwnerContext ownerContext;

    public MessagingController(MessagingService messaging, AiProcessingService ai, OwnerContext ownerContext) {
        this.messaging = messaging;
        this.ai = ai;
        this.ownerContext = ownerContext;
    }

    @GetMapping("/conversations")
    public List<MessagingApi.ConversationSummary> list() { return messaging.listConversations(); }

    @GetMapping("/conversations/{id}")
    public MessagingApi.ConversationDetail get(@PathVariable UUID id) { return messaging.getConversation(id); }

    @PostMapping("/conversations/{id}/send")
    public MessagingApi.MessageResponse send(@PathVariable UUID id, @Valid @RequestBody MessagingApi.SendRequest request) {
        return messaging.send(id, request);
    }

    @PostMapping("/messages/{messageId}/ai-suggestion")
    public MessagingApi.AiSuggestionResponse regenerate(@PathVariable UUID messageId) {
        return ai.generateForMessage(messageId, ownerContext.currentOwnerId());
    }
}
