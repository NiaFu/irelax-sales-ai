package com.irelax.salesai.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "knowledge_entries")
public class KnowledgeEntry extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, length = 128)
    private String ownerId;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "source_url", length = 600)
    private String sourceUrl;

    @Column(nullable = false)
    private boolean active = true;

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
