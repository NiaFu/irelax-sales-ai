package com.irelax.salesai.service;

import com.irelax.salesai.api.dto.ProductApi;
import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.common.ResourceNotFoundException;
import com.irelax.salesai.domain.Product;
import com.irelax.salesai.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository products;
    private final OwnerContext ownerContext;

    public ProductService(ProductRepository products, OwnerContext ownerContext) {
        this.products = products;
        this.ownerContext = ownerContext;
    }

    @Transactional(readOnly = true)
    public List<ProductApi.Response> list(boolean includeInactive) {
        String ownerId = ownerContext.currentOwnerId();
        List<Product> result = includeInactive ? products.findByOwnerIdOrderByNameAsc(ownerId) : products.findByOwnerIdAndActiveTrueOrderByNameAsc(ownerId);
        return result.stream().map(this::map).toList();
    }

    @Transactional
    public ProductApi.Response create(ProductApi.UpsertRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        if (products.findByOwnerIdAndNameIgnoreCase(ownerId, request.name().trim()).isPresent()) {
            throw new IllegalStateException("A product already exists with this name");
        }
        Product product = new Product();
        product.setOwnerId(ownerId);
        apply(product, request);
        return map(products.save(product));
    }

    @Transactional
    public ProductApi.Response update(UUID id, ProductApi.UpsertRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        Product product = products.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        apply(product, request);
        return map(products.save(product));
    }

    private void apply(Product p, ProductApi.UpsertRequest r) {
        p.setName(r.name().trim());
        p.setBrand(blankToNull(r.brand()));
        p.setModel(blankToNull(r.model()));
        p.setPrice(r.price());
        p.setWarrantyYears(r.warrantyYears());
        p.setDescription(blankToNull(r.description()));
        p.setFeatures(blankToNull(r.features()));
        p.setStrengths(blankToNull(r.strengths()));
        p.setProductUrl(blankToNull(r.productUrl()));
        p.setManualUrl(blankToNull(r.manualUrl()));
        p.setActive(r.active() == null || r.active());
    }

    private ProductApi.Response map(Product p) {
        return new ProductApi.Response(p.getId(), p.getName(), p.getBrand(), p.getModel(), p.getPrice(), p.getWarrantyYears(),
                p.getDescription(), p.getFeatures(), p.getStrengths(), p.getProductUrl(), p.getManualUrl(), p.isActive());
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
