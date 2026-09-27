package com.irelax.salesai.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(name = "uk_products_owner_name", columnNames = {"owner_id", "name"}))
public class Product extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, length = 128)
    private String ownerId;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(length = 120)
    private String model;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "warranty_years")
    private Integer warrantyYears;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String features;

    @Column(columnDefinition = "text")
    private String strengths;

    @Column(name = "product_url", length = 600)
    private String productUrl;

    @Column(name = "manual_url", length = 600)
    private String manualUrl;

    @Column(nullable = false)
    private boolean active = true;

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getWarrantyYears() { return warrantyYears; }
    public void setWarrantyYears(Integer warrantyYears) { this.warrantyYears = warrantyYears; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public String getManualUrl() { return manualUrl; }
    public void setManualUrl(String manualUrl) { this.manualUrl = manualUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
