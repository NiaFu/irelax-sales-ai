package com.irelax.salesai.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "sales_assistant_profiles", uniqueConstraints = @UniqueConstraint(name = "uk_profile_owner", columnNames = "owner_id"))
public class SalesAssistantProfile extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, length = 128)
    private String ownerId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "text")
    private String tone;

    @Column(columnDefinition = "text")
    private String rules;

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
}
