package com.irelax.salesai.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(name = "uk_customers_owner_phone", columnNames = {"owner_id", "phone"}))
public class Customer extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, length = 128)
    private String ownerId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_channel", nullable = false, length = 30)
    private ContactChannel preferredChannel = ContactChannel.SMS;

    @Enumerated(EnumType.STRING)
    @Column(name = "sales_stage", nullable = false, length = 30)
    private SalesStage salesStage = SalesStage.NEW;

    @Column(name = "first_visit_date")
    private LocalDate firstVisitDate;

    @Column(name = "first_visit_location", length = 120)
    private String firstVisitLocation;

    @Column(columnDefinition = "text")
    private String feedback;

    @Column(name = "budget_min", precision = 12, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 12, scale = 2)
    private BigDecimal budgetMax;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "last_contact_at")
    private Instant lastContactAt;

    @Column(name = "next_follow_up_at")
    private Instant nextFollowUpAt;

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public ContactChannel getPreferredChannel() { return preferredChannel; }
    public void setPreferredChannel(ContactChannel preferredChannel) { this.preferredChannel = preferredChannel; }
    public SalesStage getSalesStage() { return salesStage; }
    public void setSalesStage(SalesStage salesStage) { this.salesStage = salesStage; }
    public LocalDate getFirstVisitDate() { return firstVisitDate; }
    public void setFirstVisitDate(LocalDate firstVisitDate) { this.firstVisitDate = firstVisitDate; }
    public String getFirstVisitLocation() { return firstVisitLocation; }
    public void setFirstVisitLocation(String firstVisitLocation) { this.firstVisitLocation = firstVisitLocation; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }
    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getLastContactAt() { return lastContactAt; }
    public void setLastContactAt(Instant lastContactAt) { this.lastContactAt = lastContactAt; }
    public Instant getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(Instant nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }
}
