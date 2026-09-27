package com.irelax.salesai.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "customer_product_interests", uniqueConstraints = @UniqueConstraint(name = "uk_customer_product_source", columnNames = {"owner_id", "customer_id", "product_id", "source"}))
public class CustomerProductInterest extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, length = 128)
    private String ownerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_level", nullable = false, length = 20)
    private InterestLevel interestLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterestSource source;

    @Column(precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(columnDefinition = "text")
    private String reason;

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public InterestLevel getInterestLevel() { return interestLevel; }
    public void setInterestLevel(InterestLevel interestLevel) { this.interestLevel = interestLevel; }
    public InterestSource getSource() { return source; }
    public void setSource(InterestSource source) { this.source = source; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
