package com.irelax.salesai.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_suggestions", indexes = @Index(name = "idx_ai_suggestions_message", columnList = "message_id,created_at"))
public class AiSuggestion extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, length = 128)
    private String ownerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "draft_reply", nullable = false, columnDefinition = "text")
    private String draftReply;

    @Column(length = 80)
    private String intent;

    @Column(length = 40)
    private String sentiment;

    @Column(name = "detected_products", columnDefinition = "text")
    private String detectedProducts;

    @Column(name = "follow_up_required", nullable = false)
    private boolean followUpRequired;

    @Column(name = "suggested_follow_up_at")
    private Instant suggestedFollowUpAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_suggestion", length = 30)
    private SalesStage stageSuggestion;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiSuggestionStatus status = AiSuggestionStatus.PENDING;

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    public String getDraftReply() { return draftReply; }
    public void setDraftReply(String draftReply) { this.draftReply = draftReply; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public String getDetectedProducts() { return detectedProducts; }
    public void setDetectedProducts(String detectedProducts) { this.detectedProducts = detectedProducts; }
    public boolean isFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(boolean followUpRequired) { this.followUpRequired = followUpRequired; }
    public Instant getSuggestedFollowUpAt() { return suggestedFollowUpAt; }
    public void setSuggestedFollowUpAt(Instant suggestedFollowUpAt) { this.suggestedFollowUpAt = suggestedFollowUpAt; }
    public SalesStage getStageSuggestion() { return stageSuggestion; }
    public void setStageSuggestion(SalesStage stageSuggestion) { this.stageSuggestion = stageSuggestion; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public AiSuggestionStatus getStatus() { return status; }
    public void setStatus(AiSuggestionStatus status) { this.status = status; }
}
