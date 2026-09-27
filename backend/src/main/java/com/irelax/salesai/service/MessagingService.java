package com.irelax.salesai.service;

import com.irelax.salesai.api.dto.MessagingApi;
import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.common.ResourceNotFoundException;
import com.irelax.salesai.domain.*;
import com.irelax.salesai.integration.twilio.TwilioGateway;
import com.irelax.salesai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MessagingService {
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final AiSuggestionRepository suggestions;
    private final CustomerRepository customers;
    private final CustomerService customerService;
    private final OwnerContext ownerContext;
    private final TwilioGateway twilioGateway;
    private final AiSuggestionMapper suggestionMapper;

    public MessagingService(ConversationRepository conversations, MessageRepository messages,
                            AiSuggestionRepository suggestions, CustomerRepository customers,
                            CustomerService customerService, OwnerContext ownerContext,
                            TwilioGateway twilioGateway, AiSuggestionMapper suggestionMapper) {
        this.conversations = conversations;
        this.messages = messages;
        this.suggestions = suggestions;
        this.customers = customers;
        this.customerService = customerService;
        this.ownerContext = ownerContext;
        this.twilioGateway = twilioGateway;
        this.suggestionMapper = suggestionMapper;
    }

    @Transactional(readOnly = true)
    public List<MessagingApi.ConversationSummary> listConversations() {
        String ownerId = ownerContext.currentOwnerId();
        return conversations.findByOwnerIdOrderByLastActivityAtDesc(ownerId).stream()
                .map(c -> summary(ownerId, c)).toList();
    }

    @Transactional(readOnly = true)
    public MessagingApi.ConversationDetail getConversation(UUID id) {
        String ownerId = ownerContext.currentOwnerId();
        Conversation conversation = conversations.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        List<MessagingApi.MessageResponse> messageList = messages.findByConversationIdAndOwnerIdOrderByCreatedAtAsc(id, ownerId)
                .stream().map(this::mapMessage).toList();
        MessagingApi.AiSuggestionResponse latest = suggestions.findFirstByOwnerIdAndMessageConversationIdOrderByCreatedAtDesc(ownerId, id)
                .map(suggestionMapper::map).orElse(null);
        return new MessagingApi.ConversationDetail(summary(ownerId, conversation), messageList, latest);
    }

    @Transactional
    public Message receiveInbound(String ownerId, String from, String to, String content, String externalSid) {
        if (externalSid != null && !externalSid.isBlank()) {
            Message existing = messages.findByExternalMessageId(externalSid).orElse(null);
            if (existing != null) return existing;
        }
        Customer customer = customerService.findOrCreateFromPhone(ownerId, from);
        Conversation conversation = conversations.findByOwnerIdAndCustomerIdAndChannel(ownerId, customer.getId(), ContactChannel.SMS)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setOwnerId(ownerId);
                    c.setCustomer(customer);
                    c.setChannel(ContactChannel.SMS);
                    c.setLastActivityAt(Instant.now());
                    return conversations.save(c);
                });

        Instant now = Instant.now();
        Message message = new Message();
        message.setOwnerId(ownerId);
        message.setConversation(conversation);
        message.setDirection(MessageDirection.INBOUND);
        message.setSender(from);
        message.setReceiver(to);
        message.setContent(content == null ? "" : content.trim());
        message.setStatus(MessageStatus.RECEIVED);
        message.setExternalMessageId(externalSid);
        message.setReceivedAt(now);
        message = messages.save(message);

        customer.setLastContactAt(now);
        customers.save(customer);
        conversation.setLastActivityAt(now);
        conversations.save(conversation);
        return message;
    }

    @Transactional
    public MessagingApi.MessageResponse send(UUID conversationId, MessagingApi.SendRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        Conversation conversation = conversations.findByIdAndOwnerId(conversationId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        Customer customer = conversation.getCustomer();
        Instant now = Instant.now();

        Message message = new Message();
        message.setOwnerId(ownerId);
        message.setConversation(conversation);
        message.setDirection(MessageDirection.OUTBOUND);
        message.setSender("IRELAX");
        message.setReceiver(customer.getPhone());
        message.setContent(request.content().trim());
        message.setStatus(MessageStatus.DRAFT);
        message = messages.save(message);

        try {
            TwilioGateway.SendResult result = twilioGateway.sendSms(customer.getPhone(), message.getContent());
            message.setExternalMessageId(result.sid());
            message.setStatus(mapTwilioStatus(result.status()));
            message.setSentAt(now);
        } catch (RuntimeException e) {
            message.setStatus(MessageStatus.FAILED);
            message.setErrorCode("SEND_ERROR");
            messages.save(message);
            throw e;
        }

        if (request.suggestionId() != null) {
            suggestions.findByIdAndOwnerId(request.suggestionId(), ownerId).ifPresent(suggestion -> {
                if (suggestion.getMessage().getConversation().getId().equals(conversationId)) {
                    suggestion.setStatus(AiSuggestionStatus.USED);
                    suggestions.save(suggestion);
                }
            });
        }
        customer.setLastContactAt(now);
        if (customer.getSalesStage() == SalesStage.NEW) customer.setSalesStage(SalesStage.CONTACTED);
        customers.save(customer);
        conversation.setLastActivityAt(now);
        conversations.save(conversation);
        return mapMessage(messages.save(message));
    }

    @Transactional
    public void updateDeliveryStatus(String externalSid, String twilioStatus, String errorCode) {
        if (externalSid == null || externalSid.isBlank()) return;
        messages.findByExternalMessageId(externalSid).ifPresent(message -> {
            message.setStatus(mapTwilioStatus(twilioStatus));
            message.setErrorCode(errorCode == null || errorCode.isBlank() ? null : errorCode);
            messages.save(message);
        });
    }

    public MessagingApi.MessageResponse mapMessage(Message m) {
        return new MessagingApi.MessageResponse(m.getId(), m.getDirection(), m.getSender(), m.getReceiver(), m.getContent(),
                m.getStatus(), m.getExternalMessageId(), m.getErrorCode(), m.getSentAt(), m.getReceivedAt(), m.getCreatedAt());
    }

    public MessagingApi.ConversationSummary summary(String ownerId, Conversation c) {
        Message last = messages.findFirstByConversationIdAndOwnerIdOrderByCreatedAtDesc(c.getId(), ownerId).orElse(null);
        Customer customer = c.getCustomer();
        String name = (customer.getFirstName() + " " + (customer.getLastName() == null ? "" : customer.getLastName())).trim();
        return new MessagingApi.ConversationSummary(c.getId(), customer.getId(), name, customer.getPhone(), customer.getSalesStage(),
                c.getChannel(), last == null ? "" : last.getContent(), last == null ? null : last.getDirection(),
                last == null ? c.getLastActivityAt() : messageTime(last));
    }

    private Instant messageTime(Message m) {
        if (m.getReceivedAt() != null) return m.getReceivedAt();
        if (m.getSentAt() != null) return m.getSentAt();
        return m.getCreatedAt();
    }

    private MessageStatus mapTwilioStatus(String status) {
        if (status == null) return MessageStatus.SENT;
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "queued", "accepted", "scheduled" -> MessageStatus.QUEUED;
            case "sending", "sent" -> MessageStatus.SENT;
            case "delivered", "read" -> MessageStatus.DELIVERED;
            case "undelivered" -> MessageStatus.UNDELIVERED;
            case "failed", "canceled" -> MessageStatus.FAILED;
            default -> MessageStatus.SENT;
        };
    }
}
